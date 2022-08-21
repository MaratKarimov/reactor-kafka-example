package ru.maratk.reactor.kafka.example.consumer.app.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import ru.maratk.reactor.kafka.example.consumer.app.exception.ReceiverRecordException;
import ru.maratk.reactor.kafka.example.consumer.app.retry.ControlledRetry;
import ru.maratk.reactor.kafka.example.consumer.app.service.TaskService;
import ru.maratk.reactor.kafka.example.core.lib.Task;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TaskConsumer {

    private final Integer maxRetries;

    private final KafkaReceiver<String, Task> kafkaReceiver;
    private final TaskService taskService;

    private final ControlledRetry controlledRetry;

    private final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    public TaskConsumer(@Value(value = "${max.retries}") final Integer maxRetries
            , @Autowired final KafkaReceiver<String, Task> kafkaReceiver
            , @Autowired final TaskService taskService
            , @Autowired final ControlledRetry controlledRetry
            , @Autowired final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        this.maxRetries = maxRetries;
        this.kafkaReceiver = kafkaReceiver;
        this.taskService = taskService;
        this.controlledRetry = controlledRetry;
        this.deadLetterPublishingRecoverer = deadLetterPublishingRecoverer;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void consume() {
        // https://stackoverflow.com/questions/54984724/reactor-kafka-at-least-once-handling-failures-and-offsets-in-multi-partition
        Flux.defer(kafkaReceiver::receive)
                .groupBy(m -> m.receiverOffset().topicPartition())
                .publishOn(scheduler)
                .flatMap(r -> r.publishOn(scheduler).concatMap(processPartitions()))
                .retry()
                .subscribe();
    }

    private Function<ReceiverRecord<String, Task>, Publisher<?>> processPartitions() {
        return flux -> Flux
                .just(flux)
                .concatMap(processPartition())
                .retryWhen(controlledRetry)
                .onErrorResume(onPartitionError());
    }

    private Function<ReceiverRecord<String, Task>, Publisher<?>> processPartition() {
        return rr -> {
            logger.info("Start process message with key: {} offset: {} partition: {}"
                    , rr.key()
                    , rr.receiverOffset().offset()
                    , rr.receiverOffset().topicPartition().partition());
            return taskService.process(rr.value())
                    .then(rr.receiverOffset().commit())
                    .doOnSuccess(logOnPartitionSuccess(rr))
                    .doOnError(e -> {
                        final String topicPartition = rr.receiverOffset().topicPartition() != null
                                ? Integer.toString(rr.receiverOffset().topicPartition().partition()) : "unknown";
                        logger.info("Committing offset failed for key {}: offset: {} partition: {}"
                                , rr.key()
                                , rr.receiverOffset().offset()
                                , topicPartition);
                        throw Exceptions.propagate(new ReceiverRecordException(rr, new RuntimeException("test")));
                    });
        };
    }


    private Consumer logOnPartitionSuccess(final ReceiverRecord<String, Task> rr){
        return r -> logger.info("Committing key {}: offset: {} partition: {}"
                , rr.key()
                , rr.receiverOffset().offset()
                , rr.receiverOffset().topicPartition().partition());
    }

    private Function<Throwable, Publisher<?>> onPartitionError(){
        return (e) -> {
            final ReceiverRecordException ex = (ReceiverRecordException) e;
            final ReceiverRecord<String, Task> receiverRecord = ex.getRecord();
            if(receiverRecord.value().findSafelyInternalResendCounter() < maxRetries) {
                logger.error("Fast retries exhausted for key {}: offset: {} partition: {}"
                        , receiverRecord.key()
                        , receiverRecord.receiverOffset().offset()
                        , receiverRecord.receiverOffset().topicPartition().partition()
                        , ex);
                deadLetterPublishingRecoverer.accept(incrementInternalResendCounter(receiverRecord), ex);
            } else {
                logger.error("Slow retries exhausted for key: {}, offset: {}, partition: {}"
                        , receiverRecord.key()
                        , receiverRecord.receiverOffset().offset()
                        , receiverRecord.receiverOffset().topicPartition().partition()
                        , ex);
            }
            ex.getRecord().receiverOffset().acknowledge();
            return Mono.empty();
        };
    }

    private ConsumerRecord<String, Task> incrementInternalResendCounter(final ConsumerRecord<String, Task> c){
        final Task task = c.value();
        final Task updatedTask = Task.TaskBuilder.aTask()
                .withName(task.getName())
                .withPriority(task.getPriority())
                .withInternalResendCounter(task.findSafelyInternalResendCounter() + 1)
                .build();
        return new ConsumerRecord<>(c.topic(), c.partition(), c.offset(), c.key(), updatedTask);
    }
}
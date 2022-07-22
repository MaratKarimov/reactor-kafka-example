package ru.maratk.reactor.kafka.example.consumer.app.consumer;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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

    private final KafkaReceiver<String, Task> kafkaReceiver;
    private final TaskService taskService;

    private final ControlledRetry controlledRetry;

    private final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    @Autowired
    public TaskConsumer(final KafkaReceiver<String, Task> kafkaReceiver
            , final TaskService taskService
            , final ControlledRetry controlledRetry
            , final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
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
                .doOnError(onPartitionError());
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
                    .doOnError(logOnPartitionError(rr));
        };
    }


    private Consumer logOnPartitionSuccess(final ReceiverRecord<String, Task> rr){
        return r -> logger.info("Committing key {}: offset: {} partition: {}"
                , rr.key()
                , rr.receiverOffset().offset()
                , rr.receiverOffset().topicPartition().partition());
    }

    private Consumer logOnPartitionError(final ReceiverRecord<String, Task> rr){
        return r -> logger.info("Committing offset failed for key {}: offset: {} partition: {}"
                , rr.key()
                , rr.receiverOffset().offset()
                , rr.receiverOffset().topicPartition().partition());
    }

    private Consumer<Throwable> onPartitionError(){
        return (e) -> {
            final ReceiverRecordException ex = (ReceiverRecordException) e;
            logger.error("Retries exhausted for key {}: offset: {} partition: {}"
                    , ex.getRecord().key()
                    , ex.getRecord().receiverOffset().offset()
                    , ex.getRecord().receiverOffset().topicPartition().partition()
                    , ex);
            deadLetterPublishingRecoverer.accept(ex.getRecord(), ex);
            ex.getRecord().receiverOffset().acknowledge();
        };
    }
}
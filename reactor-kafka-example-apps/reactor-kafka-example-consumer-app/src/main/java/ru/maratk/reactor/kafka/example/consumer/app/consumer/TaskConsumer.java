package ru.maratk.reactor.kafka.example.consumer.app.consumer;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;
import ru.maratk.reactor.kafka.example.core.lib.Task;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TaskConsumer {

    private final KafkaReceiver<String, Task> kafkaReceiver;

    private final int numRetries = 3;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    @Autowired
    public TaskConsumer(final KafkaReceiver<String, Task> kafkaReceiver) {
        this.kafkaReceiver = kafkaReceiver;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void consume() {
        // https://stackoverflow.com/questions/54984724/reactor-kafka-at-least-once-handling-failures-and-offsets-in-multi-partition
        Flux.defer(kafkaReceiver::receive)
                .groupBy(m -> m.receiverOffset().topicPartition())
                .publishOn(scheduler)
                .flatMap(r -> r.publishOn(scheduler).concatMap(processPartitions()))
                .doOnError(donOnPartitionsError())
                .retry()
                .subscribe();
    }

    private Function<ReceiverRecord<String, Task>, Publisher<?>> processPartitions() {
        return flux -> Flux
                .just(flux)
                .concatMap(processPartition())
                .retryWhen(Retry.fixedDelay(numRetries, Duration.ofSeconds(10)));
    }

    private Function<ReceiverRecord<String, Task>, Publisher<?>> processPartition() {
        return rr -> {
            logger.info("Start process message with order: {} offset: {} partition: {}"
                    , rr.key()
                    , rr.receiverOffset().offset()
                    , rr.receiverOffset().topicPartition().partition());
            return process(rr)
                    .then(rr.receiverOffset().commit())
                    .doOnSuccess(logOnPartitionSuccess(rr))
                    .doOnError(logOnPartitionError(rr));
        };
    }

    private Mono<Void> process(final ReceiverRecord<String, Task> rr){
        try { Thread.sleep(5000L); }
        catch (final InterruptedException e) { logger.error("Interrupted thread", e); }
        return Mono.empty();
    }

    private Consumer logOnPartitionSuccess(final ReceiverRecord<String, Task> rr){
        return r -> logger.info("Committing order {}: offset: {} partition: {} "
                , rr.key()
                , rr.receiverOffset().offset()
                , rr.receiverOffset().topicPartition().partition());
    }

    private Consumer logOnPartitionError(final ReceiverRecord<String, Task> rr){
        return r -> logger.info("Committing offset failed for order {}: offset: {} partition: {} "
                , rr.key()
                , rr.receiverOffset().offset()
                , rr.receiverOffset().topicPartition().partition());
    }

    private Consumer<? super Throwable> donOnPartitionsError(){
        return e -> {
            logger.info("Moving to next message because of error", e);
            try { Thread.sleep(5000); }
            catch (final InterruptedException e1) { e1.printStackTrace(); }
        };
    }
}
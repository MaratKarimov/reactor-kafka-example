package ru.maratk.reactor.kafka.example.consumer.app.retry;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Component
public final class ControlledRetry extends Retry {

    private final int numRetries;
    private final Long fastPauseMs;

    public ControlledRetry(@Value("${num.retries}") final int numRetries
            , @Value("${fast.pause.ms}") final Long fastPauseMs) {
        this.numRetries = numRetries;
        this.fastPauseMs = fastPauseMs;
    }

    @Override
    public Publisher<?> generateCompanion(final Flux<RetrySignal> retrySignals) {
        return retrySignals.map(rs -> getNumberOfTries(rs));
    }

    private Long getNumberOfTries(final Retry.RetrySignal rs) {
        try { Thread.sleep(fastPauseMs); } catch (final InterruptedException e) {}
        if (rs.totalRetries() < numRetries) { return rs.totalRetries(); }
        else { throw Exceptions.propagate(rs.failure()); }
    }
}
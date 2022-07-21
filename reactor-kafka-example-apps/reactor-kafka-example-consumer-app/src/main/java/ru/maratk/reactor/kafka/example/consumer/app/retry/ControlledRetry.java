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

    public ControlledRetry(@Value("${num.retries}") final int numRetries) {
        this.numRetries = numRetries;
    }

    @Override
    public Publisher<?> generateCompanion(final Flux<RetrySignal> retrySignals) {
        return retrySignals.map(rs -> getNumberOfTries(rs));
    }

    private Long getNumberOfTries(final Retry.RetrySignal rs) {
        if (rs.totalRetries() < numRetries) { return rs.totalRetries(); }
        else { throw Exceptions.propagate(rs.failure()); }
    }
}
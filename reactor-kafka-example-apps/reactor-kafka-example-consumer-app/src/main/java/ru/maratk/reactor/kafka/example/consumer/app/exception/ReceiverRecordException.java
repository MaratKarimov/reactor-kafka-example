package ru.maratk.reactor.kafka.example.consumer.app.exception;

import reactor.kafka.receiver.ReceiverRecord;

public final class ReceiverRecordException extends RuntimeException {

    private final ReceiverRecord record;

    public ReceiverRecordException(final ReceiverRecord record, Throwable t) {
        super(t);
        this.record = record;
    }

    public ReceiverRecord getRecord() {
        return this.record;
    }
}
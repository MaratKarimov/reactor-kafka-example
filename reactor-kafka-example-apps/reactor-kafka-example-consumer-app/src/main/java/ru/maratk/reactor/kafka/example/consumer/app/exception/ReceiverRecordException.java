package ru.maratk.reactor.kafka.example.consumer.app.exception;

import reactor.kafka.receiver.ReceiverRecord;
import ru.maratk.reactor.kafka.example.core.lib.Task;

public final class ReceiverRecordException extends RuntimeException {

    private final ReceiverRecord record;

    public ReceiverRecordException(final ReceiverRecord<String, Task> record, final Throwable t) {
        super(t);
        this.record = record;
    }

    public ReceiverRecord<String, Task> getRecord() {
        return this.record;
    }
}
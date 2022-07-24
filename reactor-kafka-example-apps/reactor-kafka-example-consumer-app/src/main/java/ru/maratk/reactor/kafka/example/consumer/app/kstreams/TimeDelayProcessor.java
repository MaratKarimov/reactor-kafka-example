package ru.maratk.reactor.kafka.example.consumer.app.kstreams;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.time.Instant;

public class TimeDelayProcessor implements Processor<byte[], byte[], byte[], byte[]> {

    private ProcessorContext processorContext;

    private final Long slowPauseMs;

    public TimeDelayProcessor(final Long slowPauseMs) {
        this.slowPauseMs = slowPauseMs;
    }

    @Override
    public void init(final ProcessorContext<byte[], byte[]> context) {
        Processor.super.init(context);
        processorContext = context;
    }

    @Override
    public void process(final Record<byte[], byte[]> record) {
        final Long recordTimestamp = record.timestamp();
        final Long nowTimestamp = Instant.now().toEpochMilli();
        final Long diff = recordTimestamp - (nowTimestamp - slowPauseMs);
        if (diff > 0) {
            try { Thread.sleep(diff); }
            catch (final InterruptedException e) { throw new RuntimeException(e); }
        }
        processorContext.forward(record);
        processorContext.commit();
    }
}
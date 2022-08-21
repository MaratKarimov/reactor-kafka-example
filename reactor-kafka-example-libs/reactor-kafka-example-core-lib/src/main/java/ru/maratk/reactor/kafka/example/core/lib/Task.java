package ru.maratk.reactor.kafka.example.core.lib;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Task.TaskBuilder.class)
public final class Task {

    private final String name;
    private final Integer priority;

    private final Integer internalResendCounter;

    public Task(final String name
            , final Integer priority
            , final Integer internalResendCounter) {
        this.name = name;
        this.priority = priority;
        this.internalResendCounter = internalResendCounter;
    }

    public String getName() { return name; }

    public Integer getPriority() { return priority; }

    public Integer getInternalResendCounter() { return internalResendCounter; }

    public int findSafelyInternalResendCounter(){
        return internalResendCounter == null ? 0 : internalResendCounter;
    }

    @JsonPOJOBuilder
    public static final class TaskBuilder {
        private String name;
        private Integer priority;

        private Integer internalResendCounter;

        private TaskBuilder() {
        }

        public static TaskBuilder aTask() {
            return new TaskBuilder();
        }

        public TaskBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public TaskBuilder withPriority(final Integer priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder withInternalResendCounter(final Integer internalResendCounter) {
            this.internalResendCounter = internalResendCounter;
            return this;
        }

        public Task build() {
            return new Task(name, priority, internalResendCounter);
        }
    }
}
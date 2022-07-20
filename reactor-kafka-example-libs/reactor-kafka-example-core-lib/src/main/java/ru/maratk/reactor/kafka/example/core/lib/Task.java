package ru.maratk.reactor.kafka.example.core.lib;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Task.TaskBuilder.class)
public final class Task {

    private final String name;
    private final Integer priority;

    public Task(final String name, final Integer priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() { return name; }

    public Integer getPriority() { return priority; }


    @JsonPOJOBuilder
    public static final class TaskBuilder {
        private String name;
        private Integer priority;

        private TaskBuilder() {
        }

        public static TaskBuilder aTask() {
            return new TaskBuilder();
        }

        public TaskBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskBuilder withPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Task build() {
            return new Task(name, priority);
        }
    }
}
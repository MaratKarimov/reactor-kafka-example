package ru.maratk.reactor.kafka.example.consumer.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.maratk.reactor.kafka.example.core.lib.Task;

@Component
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    public Mono<Void> process(final Task t){
        return Mono.error(new RuntimeException());
    }
}
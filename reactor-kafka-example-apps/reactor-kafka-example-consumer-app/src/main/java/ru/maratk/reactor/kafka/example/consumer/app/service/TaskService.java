package ru.maratk.reactor.kafka.example.consumer.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.maratk.reactor.kafka.example.core.lib.Task;
import ru.maratk.reactor.kafka.example.dao.TaskDao;

@Component
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskDao taskDao;

    public TaskService(final TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public Mono<Void> process(final Task t){
        return taskDao.insert(t)
                .doOnSuccess(o -> { logger.info("Successfully save task in db. Updated rows counter: {}", o); Mono.empty(); })
                .onErrorContinue(Exception.class, (e, o) -> logger.error("Can't save task in db", e)).then(Mono.error(new RuntimeException()));
    }
}
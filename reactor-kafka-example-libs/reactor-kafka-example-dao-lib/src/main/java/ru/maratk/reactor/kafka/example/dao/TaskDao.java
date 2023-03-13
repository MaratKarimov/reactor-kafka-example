package ru.maratk.reactor.kafka.example.dao;

import org.jooq.DSLContext;
import org.jooq.conf.ParamType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.maratk.reactor.kafka.example.jooq.tables.Task;

public class TaskDao {
  private final DSLContext dslContext;

    public TaskDao(final DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Mono<Integer> insert(final ru.maratk.reactor.kafka.example.core.lib.Task task){
      return Mono.fromDirect(dslContext.insertInto(Task.TASK)
                .columns(Task.TASK.NAME, Task.TASK.PRIORITY, Task.TASK.INTERNAL_RESEND_COUNTER)
                .values(task.getName(), task.getPriority(), task.findSafelyInternalResendCounter()));
    }
}
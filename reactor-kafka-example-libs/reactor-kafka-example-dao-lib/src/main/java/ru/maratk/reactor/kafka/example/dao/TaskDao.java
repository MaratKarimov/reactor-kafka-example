package ru.maratk.reactor.kafka.example.dao;

import org.jooq.DSLContext;
import org.jooq.conf.ParamType;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import ru.maratk.reactor.kafka.example.jooq.tables.Task;

public class TaskDao {
    private final DatabaseClient databaseClient;
    private final DSLContext dslContext;

    public TaskDao(final DatabaseClient databaseClient, final DSLContext dslContext) {
        this.databaseClient = databaseClient;
        this.dslContext = dslContext;
    }

    public Mono<Integer> insert(final ru.maratk.reactor.kafka.example.core.lib.Task task){
        final String sql = dslContext.insertInto(Task.TASK)
                .columns(Task.TASK.NAME, Task.TASK.PRIORITY)
                .values(task.getName(), task.getPriority())
                .getSQL(ParamType.INLINED);
        return databaseClient
                .sql(sql)
                .fetch()
                .rowsUpdated();
    }
}
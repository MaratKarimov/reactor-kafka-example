package ru.maratk.reactor.kafka.example.consumer.app.config;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.kafka.common.TopicPartition;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.r2dbc.core.DatabaseClient;
import ru.maratk.reactor.kafka.example.dao.TaskDao;

@Configuration
@Import({KafkaConsumerConfig.class, KafkaProducerConfig.class, KafkaStreamsConfig.class, PostgreSQLConnectionFactoryConfig.class})
public class AppConfig {

    @Value("${task.dlq.topic1}")
    private String deadLetterTopic;

    @Bean
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(final KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> new TopicPartition(deadLetterTopic, -1));
    }

    @Bean
    TaskDao taskDao(final ConnectionFactory connectionFactory){
        return new TaskDao(DSL.using(connectionFactory));
    }
}
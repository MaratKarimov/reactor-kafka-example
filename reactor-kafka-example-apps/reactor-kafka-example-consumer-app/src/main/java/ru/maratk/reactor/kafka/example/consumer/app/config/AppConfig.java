package ru.maratk.reactor.kafka.example.consumer.app.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
@Import({KafkaConsumerConfig.class, KafkaProducerConfig.class})
public class AppConfig {

    @Value("${task.dlq.topic}")
    private String deadLetterTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Bean
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> new TopicPartition(deadLetterTopic, -1));
    }
}
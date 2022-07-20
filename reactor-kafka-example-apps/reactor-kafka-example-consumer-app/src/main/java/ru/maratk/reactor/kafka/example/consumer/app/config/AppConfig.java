package ru.maratk.reactor.kafka.example.consumer.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({KafkaConsumer.class})
public class AppConfig {
}
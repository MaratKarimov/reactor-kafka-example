package ru.maratk.reactor.kafka.example.consumer.app.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServers;

    @Value(value = "${task.topic}")
    private String topic;

    @Bean
    ProducerFactory<String, Object> producerFactory() {
        final Map<String, Object> properties = new HashMap() {{
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "reactor-kafka-example-consumer-app_" + UUID.randomUUID());
            put(ProducerConfig.ACKS_CONFIG, "all");
            put(ProducerConfig.RETRIES_CONFIG, 4);
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        }};
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    @Primary
    KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
package ru.maratk.reactor.kafka.example.consumer.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import ru.maratk.reactor.kafka.example.core.lib.Task;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConsumerConfig {
    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServers;

    @Value(value = "${task.topic}")
    private String topic;

    @Bean
    Map<String, Object> kafkaProperties() {
        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "reactor-kafka-example-consumer-app_" + UUID.randomUUID().toString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "reactor-kafka-example-consumer-app");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    @Bean
    ReceiverOptions<String, Task> kafkaReceiverOptions() {
        return ReceiverOptions.<String, Task>create(kafkaProperties())
                .withKeyDeserializer(new StringDeserializer())
                .withValueDeserializer(new JsonDeserializer(Task.class))
                .commitInterval(Duration.ZERO)
                .commitBatchSize(0)
                .subscription(Collections.singletonList(topic));

    }

    @Bean
    KafkaReceiver<String, Task> kafkaReceiver() {
        return KafkaReceiver.create(kafkaReceiverOptions());
    }
}

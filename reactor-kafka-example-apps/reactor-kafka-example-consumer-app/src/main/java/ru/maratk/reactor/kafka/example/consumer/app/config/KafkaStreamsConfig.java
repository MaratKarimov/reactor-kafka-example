package ru.maratk.reactor.kafka.example.consumer.app.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import ru.maratk.reactor.kafka.example.consumer.app.kstreams.TimeDelayProcessor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServers;

    @Value("${slow.pause.ms}")
    private Long slowPauseMs;

    @Value(value = "${task.topic}")
    private String topic;

    @Value("${task.dlq.topic}")
    private String deadLetterTopic;

    @Bean
    Map<String, Object> kStreamsParam() {
        final Map<String, Object> params = new HashMap() {{
            put(StreamsConfig.APPLICATION_ID_CONFIG, "reactor-kafka-example-consumer-app-timedelay");
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
            put(StreamsConfig.EXACTLY_ONCE_V2, true);
        }};
        return params;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration streamsConfiguration() { return new KafkaStreamsConfiguration(kStreamsParam()); }

    TimeDelayProcessor timeDelayProcessor(){ return new TimeDelayProcessor(slowPauseMs); }

    @Bean
    Topology topology(final StreamsBuilder streamsBuilder){
        return streamsBuilder
                .build()
                .addSource("processorSource", deadLetterTopic)
                .addProcessor("processor", this::timeDelayProcessor, "processorSource")
                .addSink("sink", topic, "processor");
    }
}
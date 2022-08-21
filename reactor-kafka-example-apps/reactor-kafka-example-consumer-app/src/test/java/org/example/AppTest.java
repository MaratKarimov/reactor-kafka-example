package org.example;

import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import ru.maratk.reactor.kafka.example.consumer.app.config.AppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AppConfig.class)
public class AppTest {

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:14-alpine"));

    @ClassRule
    public static ExternalResource resource = new ExternalResource() {
        @Override
        public void before() { kafka.start(); postgres.start(); }
        @Override
        public void after() {}
    };

    @DynamicPropertySource
    static void registerProperties(final DynamicPropertyRegistry registry) {
        // kafka
        registry.add("kafka.bootstrap.servers", () -> kafka.getBootstrapServers());
        // postgres
        registry.add("storage.datasource.jdbcUrl", () -> postgres.getJdbcUrl());
        registry.add("storage.datasource.username", () -> postgres.getUsername());
        registry.add("storage.datasource.password", () -> postgres.getPassword());
    }

    @Test
    public void test() {
        assertTrue( true );
    }
}
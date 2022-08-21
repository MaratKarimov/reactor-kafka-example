package ru.maratk.reactor.kafka.example.consumer.app.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.postgresql.PGProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.Properties;

@Configuration
public class PostgreSQLConnectionFactoryConfig {

    @Value("${storage.datasource.jdbcUrl}")
    private String jdbcUrl;
    @Value("${storage.datasource.username}")
    private String username;
    @Value("${storage.datasource.password}")
    private String password;

    @Value("${storage.datasource.pool.initial-size}")
    private Integer poolInitialSize;

    @Value("${storage.datasource.pool.max-size}")
    private Integer poolMaxSize;

    @Bean
    public ConnectionFactory connectionFactory() {
        final ConnectionPoolConfiguration connectionPoolConfiguration = ConnectionPoolConfiguration
                .builder(ConnectionFactories.get(connectionFactoryOptions()))
                .initialSize(poolInitialSize)
                .maxSize(poolMaxSize)
                .build();
        return new ConnectionPool(connectionPoolConfiguration);
    }

    private ConnectionFactoryOptions connectionFactoryOptions(){
        final Properties jdbcProps = org.postgresql.Driver.parseURL(jdbcUrl, null);
        return ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "postgresql")
                .option(ConnectionFactoryOptions.USER, username)
                .option(ConnectionFactoryOptions.PASSWORD, password)
                .option(ConnectionFactoryOptions.HOST, jdbcProps.getProperty(PGProperty.PG_HOST.getName()))
                .option(ConnectionFactoryOptions.PORT, Integer.valueOf(jdbcProps.getProperty(PGProperty.PG_PORT.getName())))
                .option(ConnectionFactoryOptions.DATABASE, jdbcProps.getProperty(PGProperty.PG_DBNAME.getName()))
                .build();
    }

    @Bean
    DatabaseClient databaseClient(final ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .namedParameters(true)
                .build();
    }
}
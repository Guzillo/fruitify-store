package io.github.guzillo.fruitifystorebackend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /**
     * The container is reused between test runs when Testcontainers reuse is enabled, either with
     * {@code TESTCONTAINERS_REUSE_ENABLE=true} or {@code testcontainers.reuse.enable=true} in
     * {@code ~/.testcontainers.properties}.
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        PostgreSQLContainer container = new PostgreSQLContainer("postgres:18");
        if (org.testcontainers.utility.TestcontainersConfiguration.getInstance()
            .environmentSupportsReuse()) {
            container.withReuse(true);
        }
        return container;
    }
}

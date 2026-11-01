package io.github.guzillo.fruitifystorebackend.infrastructure.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Turns on @Scheduled jobs, such as expiring unpaid orders.
// Disabled by default to allow scale-to-zero in serverless hosting (e.g. Railway).
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class SchedulingConfig {

}

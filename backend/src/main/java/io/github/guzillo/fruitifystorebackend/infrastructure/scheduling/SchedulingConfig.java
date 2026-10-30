package io.github.guzillo.fruitifystorebackend.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Turns on @Scheduled jobs, such as expiring unpaid orders.
@Configuration
@EnableScheduling
public class SchedulingConfig {

}

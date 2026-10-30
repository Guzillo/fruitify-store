package io.github.guzillo.fruitifystorebackend;

import io.github.guzillo.fruitifystorebackend.payment.config.StripeProperties;
import io.github.guzillo.fruitifystorebackend.security.config.JwtKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtKeyProperties.class,
    StripeProperties.class})
public class FruitifyStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitifyStoreBackendApplication.class, args);
    }
}

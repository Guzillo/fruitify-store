package io.github.guzillo.fruitifystorebackend.payment.config;

import com.stripe.StripeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StripeConfig {
    private final StripeProperties properties;

    @Bean
    public StripeClient stripeClient() {
        return new StripeClient(properties.secretKey());
    }

}

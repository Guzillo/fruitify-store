package io.github.guzillo.fruitifystorebackend.payment.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(

    @Pattern(regexp = "^sk_test_[a-zA-Z0-9]{24,100}$")
    String secretKey,

    @NotNull
    String domain

) {

}



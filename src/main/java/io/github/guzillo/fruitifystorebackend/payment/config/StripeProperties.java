package io.github.guzillo.fruitifystorebackend.payment.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(

    @Pattern(regexp = "^sk_test_[a-zA-Z0-9]{24,100}$")
    @NotBlank
    @NotNull
    String secretKey,

    @Pattern(regexp = "^whsec_[a-zA-Z0-9]*")
    @NotBlank
    @NotNull
    String webhookSecret,

    @NotNull
    @NotBlank
    String domain

) {

}



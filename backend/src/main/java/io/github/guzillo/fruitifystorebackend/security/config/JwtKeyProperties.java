package io.github.guzillo.fruitifystorebackend.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * RSA key material used to sign and verify JWTs (RS256).
 *
 * <p>Both values are supplied through environment variables and are expected to be base64-encoded
 * PEM or DER keys:
 *
 * <pre>
 * JWT_PUBLIC_KEY=&lt;base64 of public.pem&gt;
 * JWT_PRIVATE_KEY=&lt;base64 of private.pem&gt;
 * </pre>
 *
 * <p>For local development and tests a raw PEM value or a {@code classpath:} location is accepted
 * as well.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtKeyProperties(@NotBlank String publicKey, @NotBlank String privateKey) {

}

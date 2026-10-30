package io.github.guzillo.fruitifystorebackend.security.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Turns the base64-encoded key material from {@link JwtKeyProperties} into RSA key beans used for
 * signing and validating JWTs.
 */
@Configuration
public class JwtKeyConfig {

    @Bean
    RSAPublicKey jwtPublicKey(JwtKeyProperties properties) {
        return RsaKeyDecoder.decodePublicKey(properties.publicKey());
    }

    @Bean
    RSAPrivateKey jwtPrivateKey(JwtKeyProperties properties) {
        return RsaKeyDecoder.decodePrivateKey(properties.privateKey());
    }

}

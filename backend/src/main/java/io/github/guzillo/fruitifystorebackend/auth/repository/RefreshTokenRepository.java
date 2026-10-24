package io.github.guzillo.fruitifystorebackend.auth.repository;

import io.github.guzillo.fruitifystorebackend.auth.model.RefreshToken;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByTokenAndExpirationTimeAfter(String token, OffsetDateTime expirationTimeAfter);

    long deleteByToken(String token);
}
package io.github.guzillo.fruitifystorebackend.auth.repository;

import io.github.guzillo.fruitifystorebackend.auth.model.RefreshToken;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByTokenAndExpirationTimeAfter(String token, OffsetDateTime expirationTimeAfter);

    @Transactional
    long deleteByToken(String token);
}
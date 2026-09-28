package io.github.guzillo.fruitifystorebackend.auth.service;

import io.github.guzillo.fruitifystorebackend.auth.exception.InvalidTokenException;
import io.github.guzillo.fruitifystorebackend.auth.model.RefreshToken;
import io.github.guzillo.fruitifystorebackend.auth.repository.RefreshTokenRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final static Duration REFRESH_TOKEN_VALIDITY = Duration.ofMinutes(60);
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateRefreshToken(final String username) {
        final User user = userRepository.findByEmail(username).orElseThrow();
        final String newToken = UUID.randomUUID().toString();
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(newToken);
        refreshToken.setExpirationTime(OffsetDateTime.now().plus(REFRESH_TOKEN_VALIDITY));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);
        return newToken;
    }

    public String validateRefreshTokenAndGetUsername(final String givenToken) {
        final RefreshToken refreshToken = refreshTokenRepository.findByTokenAndExpirationTimeAfter(givenToken, OffsetDateTime.now());
        if (refreshToken == null) {
            throw new InvalidTokenException();
        }
        return refreshToken.getUser().getEmail();
    }

    public void revokeRefreshToken(final String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}

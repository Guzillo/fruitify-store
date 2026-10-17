package io.github.guzillo.fruitifystorebackend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guzillo.fruitifystorebackend.auth.model.RefreshToken;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindOnlyNonExpiredToken() {
        User user = userRepository.saveAndFlush(
            new User("token@example.com", "encoded-password", java.util.Set.of()));
        OffsetDateTime now = OffsetDateTime.now();

        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        token.setExpirationTime(now.plusMinutes(5));
        token.setUser(user);
        refreshTokenRepository.saveAndFlush(token);

        assertThat(refreshTokenRepository.findByTokenAndExpirationTimeAfter("valid-token", now))
            .isNotNull();
        assertThat(refreshTokenRepository.findByTokenAndExpirationTimeAfter(
            "valid-token", now.plusMinutes(10))).isNull();
    }

    @Test
    void shouldDeleteTokenByValue() {
        User user = userRepository.saveAndFlush(
            new User("revoke@example.com", "encoded-password", java.util.Set.of()));
        RefreshToken token = new RefreshToken();
        token.setToken("revoke-token");
        token.setExpirationTime(OffsetDateTime.now().plusMinutes(5));
        token.setUser(user);
        refreshTokenRepository.saveAndFlush(token);

        assertThat(refreshTokenRepository.deleteByToken("revoke-token")).isEqualTo(1);
        assertThat(refreshTokenRepository.findById(token.getId())).isEmpty();
    }
}

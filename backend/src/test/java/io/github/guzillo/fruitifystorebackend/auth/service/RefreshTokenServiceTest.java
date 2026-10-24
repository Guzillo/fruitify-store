package io.github.guzillo.fruitifystorebackend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.auth.exception.InvalidTokenException;
import io.github.guzillo.fruitifystorebackend.auth.model.RefreshToken;
import io.github.guzillo.fruitifystorebackend.auth.repository.RefreshTokenRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldGenerateAndSaveRefreshToken() {
        User user = new User();
        user.setEmail("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        OffsetDateTime before = OffsetDateTime.now().plusMinutes(59);

        String token = refreshTokenService.generateRefreshToken("john@example.com");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken savedToken = captor.getValue();
        assertThat(token).isEqualTo(savedToken.getToken());
        assertThat(token).isNotBlank();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getExpirationTime()).isAfter(before);
        assertThat(savedToken.getExpirationTime()).isBefore(OffsetDateTime.now().plusMinutes(61));
    }

    @Test
    void shouldNotSaveRefreshTokenForUnknownUser() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> refreshTokenService.generateRefreshToken("unknown@example.com"))
            .isInstanceOf(java.util.NoSuchElementException.class);

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void shouldReturnUsernameForValidRefreshToken() {
        User user = new User();
        user.setEmail("john@example.com");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        when(refreshTokenRepository.findByTokenAndExpirationTimeAfter(any(), any()))
            .thenReturn(refreshToken);

        assertThat(refreshTokenService.validateRefreshTokenAndGetUsername("token"))
            .isEqualTo("john@example.com");
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        when(refreshTokenRepository.findByTokenAndExpirationTimeAfter(any(), any()))
            .thenReturn(null);
        assertThatThrownBy(() -> refreshTokenService.validateRefreshTokenAndGetUsername("invalid"))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage("Invalid token");
    }

    @Test
    void shouldRevokeRefreshToken() {
        refreshTokenService.revokeRefreshToken("token");

        verify(refreshTokenRepository).deleteByToken("token");
    }
}

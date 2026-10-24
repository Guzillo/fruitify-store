package io.github.guzillo.fruitifystorebackend.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.auth.dto.AuthenticationResponse;
import io.github.guzillo.fruitifystorebackend.auth.dto.RefreshTokenRequest;
import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.service.RefreshTokenService;
import io.github.guzillo.fruitifystorebackend.auth.service.SignupService;
import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private SignupService signupService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldAuthenticateAndReturnAccessAndRefreshTokens() {
        UserDetails user = User.withUsername("john@example.com")
            .password("encoded")
            .roles("USER")
            .build();
        when(authentication.getName()).thenReturn("john@example.com");
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken("john@example.com"))
            .thenReturn("refresh-token");

        AuthenticationResponse response = authController.authenticate(authentication);

        assertThat(response).isEqualTo(new AuthenticationResponse("access-token", "refresh-token"));
        verify(userDetailsService).loadUserByUsername("john@example.com");
        verify(tokenService).generateToken(user);
        verify(refreshTokenService).generateRefreshToken("john@example.com");
    }

    @Test
    void shouldRefreshAccessTokenAndKeepRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        UserDetails user = User.withUsername("john@example.com")
            .password("encoded")
            .roles("USER")
            .build();
        when(refreshTokenService.validateRefreshTokenAndGetUsername("refresh-token"))
            .thenReturn("john@example.com");
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn("new-access-token");

        assertThat(authController.refresh(request))
            .isEqualTo(new AuthenticationResponse("new-access-token", "refresh-token"));
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        authController.logout(new RefreshTokenRequest("refresh-token"));

        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }

    @Test
    void shouldDelegateSignupToSignupService() {
        SignupUserDto request = new SignupUserDto("john@example.com", "password");
        authController.signup(request);

        verify(signupService).signup(request);
    }
}

package io.github.guzillo.fruitifystorebackend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateTokenWithAuthenticationClaims() {
        when(authentication.getName()).thenReturn("john@example.com");
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("READ_PRIVILEGE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any())).thenReturn(jwt("access-token"));

        String token = tokenService.generateToken(authentication);

        assertThat(token).isEqualTo("access-token");
        ArgumentCaptor<JwtEncoderParameters> captor =
            ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        assertClaims(captor.getValue().getClaims(), "john@example.com", "ROLE_USER READ_PRIVILEGE");
    }

    @Test
    void shouldGenerateTokenWithUserDetailsClaims() {
        org.springframework.security.core.userdetails.User user =
            new org.springframework.security.core.userdetails.User(
                "jane@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any())).thenReturn(jwt("admin-token"));

        String token = tokenService.generateToken(user);

        assertThat(token).isEqualTo("admin-token");
        ArgumentCaptor<JwtEncoderParameters> captor =
            ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        assertClaims(captor.getValue().getClaims(), "jane@example.com", "ROLE_ADMIN");
    }

    private void assertClaims(JwtClaimsSet claims, String subject, String expectedScope) {
        assertThat(claims.getClaimAsString("iss")).isEqualTo("self");
        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.getClaimAsString("scope")).isEqualTo(expectedScope);
        assertThat(Duration.between(Instant.now(), claims.getExpiresAt())).isBetween(
            Duration.ofMinutes(59), Duration.ofMinutes(61));
    }

    private Jwt jwt(String token) {
        return Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("sub", "subject")
            .build();
    }
}

package io.github.guzillo.fruitifystorebackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1NiJ9...")
    String accessToken,
    @Schema(description = "Opaque refresh token", example = "a1b2c3d4-e5f6-7890")
    String refreshToken) {
}

package io.github.guzillo.fruitifystorebackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @Schema(description = "Refresh token returned by sign-in", example = "a1b2c3d4-e5f6-7890")
    @NotBlank String refreshToken) {
}

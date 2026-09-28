package io.github.guzillo.fruitifystorebackend.auth.dto;

public record AuthenticationResponse(String accessToken, String refreshToken) {
}

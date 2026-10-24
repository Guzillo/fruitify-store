package io.github.guzillo.fruitifystorebackend.payment.dto;

public record CheckoutSessionResponseDTO(
    String url,
    String sessionId
) {

}

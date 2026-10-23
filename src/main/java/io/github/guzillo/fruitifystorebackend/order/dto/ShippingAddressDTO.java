package io.github.guzillo.fruitifystorebackend.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressDTO(
    @Schema(description = "Recipient full name", example = "Jan Kowalski")
    @NotBlank @Size(max = 255) String recipientName,
    @Schema(description = "Street and number", example = "Marszałkowska 1")
    @NotBlank @Size(max = 255) String street,
    @Schema(description = "City", example = "Warsaw")
    @NotBlank @Size(max = 255) String city,
    @Schema(description = "Postal code", example = "00-001")
    @NotBlank @Size(max = 32) String postalCode,
    @Schema(description = "Country code or name", example = "PL")
    @NotBlank @Size(max = 255) String country
) {

}

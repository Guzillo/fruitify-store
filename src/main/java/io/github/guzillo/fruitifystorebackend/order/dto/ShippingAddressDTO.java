package io.github.guzillo.fruitifystorebackend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressDTO(
    @NotBlank @Size(max = 255) String recipientName,
    @NotBlank @Size(max = 255) String street,
    @NotBlank @Size(max = 255) String city,
    @NotBlank @Size(max = 32) String postalCode,
    @NotBlank @Size(max = 255) String country
) {

}

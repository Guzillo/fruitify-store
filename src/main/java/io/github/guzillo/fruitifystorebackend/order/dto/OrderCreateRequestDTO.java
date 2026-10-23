package io.github.guzillo.fruitifystorebackend.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequestDTO(
    @Schema(description = "Shipping address for the order")
    @NotNull @Valid ShippingAddressDTO shippingAddress
) {

}

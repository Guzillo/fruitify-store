package io.github.guzillo.fruitifystorebackend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequestDTO(
    @NotNull @Valid ShippingAddressDTO shippingAddress
) {

}

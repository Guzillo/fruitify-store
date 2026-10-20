package io.github.guzillo.fruitifystorebackend.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequestDTO(
    @NotNull @Min(1) Integer quantity
) {

}

package io.github.guzillo.fruitifystorebackend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequestDTO(
    @Schema(description = "New quantity", example = "3")
    @NotNull @Min(1) Integer quantity
) {

}

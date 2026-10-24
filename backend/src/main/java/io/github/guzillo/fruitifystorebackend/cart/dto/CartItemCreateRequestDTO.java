package io.github.guzillo.fruitifystorebackend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemCreateRequestDTO(
    @Schema(description = "Product id", example = "1")
    @NotNull Long productId,
    @Schema(description = "Quantity to add", example = "2")
    @NotNull @Min(1) Integer quantity
) {

}

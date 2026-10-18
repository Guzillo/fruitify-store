package io.github.guzillo.fruitifystorebackend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequestDTO(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 2048) String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull Boolean isActive,
    @Size(max = 255) String imageUrl,
    @NotNull Long categoryId
) {

}

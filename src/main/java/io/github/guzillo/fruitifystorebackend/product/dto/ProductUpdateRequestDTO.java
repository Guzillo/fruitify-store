package io.github.guzillo.fruitifystorebackend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record ProductUpdateRequestDTO(
    @Pattern(regexp = ".*\\S.*") @Size(max = 255) String name,
    @DecimalMin("0.00") BigDecimal price,
    @Size(max = 2048) String description,
    @Min(0) Integer stockQuantity,
    Boolean isActive,
    @Positive Long categoryId,
    @Size(max = 255) String imageUrl
) {
}

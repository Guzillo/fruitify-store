package io.github.guzillo.fruitifystorebackend.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record ProductUpdateRequestDTO(
    @Schema(description = "New product name", example = "Green apples")
    @Pattern(regexp = ".*\\S.*") @Size(max = 255) String name,
    @Schema(description = "New unit price", example = "2.99")
    @DecimalMin("0.00") BigDecimal price,
    @Schema(description = "New description", example = "Sweet green apples")
    @Size(max = 2048) String description,
    @Schema(description = "New stock quantity", example = "30")
    @Min(0) Integer stockQuantity,
    @Schema(description = "Whether the product is visible to customers", example = "true")
    Boolean isActive,
    @Schema(description = "New category id", example = "2")
    @Positive Long categoryId,
    @Schema(description = "New image URL", example = "https://example.com/green-apples.jpg")
    @Size(max = 255) String imageUrl
) {
}

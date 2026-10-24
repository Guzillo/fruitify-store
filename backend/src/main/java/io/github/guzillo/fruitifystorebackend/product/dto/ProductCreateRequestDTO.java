package io.github.guzillo.fruitifystorebackend.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequestDTO(
    @Schema(description = "Product name", example = "Apples")
    @NotBlank @Size(max = 255) String name,
    @Schema(description = "Product description", example = "Crisp red apples")
    @NotBlank @Size(max = 2048) String description,
    @Schema(description = "Unit price", example = "2.49")
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @Schema(description = "Available stock", example = "25")
    @NotNull @Min(0) Integer stockQuantity,
    @Schema(description = "Whether the product is visible to customers", example = "true")
    @NotNull Boolean isActive,
    @Schema(description = "Optional image URL", example = "https://example.com/apples.jpg")
    @Size(max = 255) String imageUrl,
    @Schema(description = "Category id", example = "1")
    @NotNull Long categoryId
) {

}

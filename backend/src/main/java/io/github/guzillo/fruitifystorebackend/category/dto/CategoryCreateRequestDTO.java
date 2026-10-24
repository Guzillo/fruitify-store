package io.github.guzillo.fruitifystorebackend.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequestDTO(
    @Schema(description = "Category name", example = "Fruit")
    @NotBlank @Size(max = 255) String name,
    @Schema(description = "URL-friendly identifier", example = "fruit")
    @NotBlank @Size(max = 255) String slug
) {

}

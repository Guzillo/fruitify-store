package io.github.guzillo.fruitifystorebackend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequestDTO(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String slug
) {

}

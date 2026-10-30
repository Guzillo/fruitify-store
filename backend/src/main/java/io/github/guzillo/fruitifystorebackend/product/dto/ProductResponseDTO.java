package io.github.guzillo.fruitifystorebackend.product.dto;

import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import java.math.BigDecimal;

public record ProductResponseDTO(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String imageUrl,
    CategoryResponseDTO category,
    Boolean isActive
) {

}

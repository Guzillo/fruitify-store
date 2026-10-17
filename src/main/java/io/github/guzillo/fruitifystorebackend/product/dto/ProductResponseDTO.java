package io.github.guzillo.fruitifystorebackend.product.dto;

import java.math.BigDecimal;

public record ProductResponseDTO(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    Boolean isActive,
    String imageUrl,
    CategoryResponseDTO category
) {

}

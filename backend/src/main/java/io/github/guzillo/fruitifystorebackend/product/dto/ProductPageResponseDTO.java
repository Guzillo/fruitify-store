package io.github.guzillo.fruitifystorebackend.product.dto;

import java.util.List;

public record ProductPageResponseDTO(
    List<ProductResponseDTO> content,
    int number,
    int size,
    long totalElements,
    int totalPages
) {

}

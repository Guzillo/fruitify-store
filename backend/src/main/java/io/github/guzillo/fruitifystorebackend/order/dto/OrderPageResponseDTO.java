package io.github.guzillo.fruitifystorebackend.order.dto;

import java.util.List;

public record OrderPageResponseDTO(
    List<OrderSummaryDTO> content,
    int number,
    int size,
    long totalElements,
    int totalPages
) {

}

package io.github.guzillo.fruitifystorebackend.order.dto;

import java.util.List;

public record AdminOrderPageResponseDTO(
    List<AdminOrderSummaryDTO> content,
    int number,
    int size,
    long totalElements,
    int totalPages
) {

}

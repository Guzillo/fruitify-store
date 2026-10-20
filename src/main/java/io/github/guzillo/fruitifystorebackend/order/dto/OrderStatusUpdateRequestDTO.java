package io.github.guzillo.fruitifystorebackend.order.dto;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequestDTO(
    @NotNull OrderStatus status
) {

}

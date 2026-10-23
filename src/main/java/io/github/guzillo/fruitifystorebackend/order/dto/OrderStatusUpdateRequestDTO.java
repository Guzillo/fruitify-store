package io.github.guzillo.fruitifystorebackend.order.dto;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequestDTO(
    @Schema(description = "Target order status", example = "PAID",
        allowableValues = {"PAID", "CANCELLED"})
    @NotNull OrderStatus status
) {

}

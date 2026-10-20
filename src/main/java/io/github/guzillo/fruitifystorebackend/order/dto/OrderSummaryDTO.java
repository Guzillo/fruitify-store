package io.github.guzillo.fruitifystorebackend.order.dto;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryDTO(
    Long id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt
) {

}

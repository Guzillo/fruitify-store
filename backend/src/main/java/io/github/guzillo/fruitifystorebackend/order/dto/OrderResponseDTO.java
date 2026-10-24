package io.github.guzillo.fruitifystorebackend.order.dto;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDTO(
    Long id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount,
    ShippingAddressDTO shippingAddress,
    List<OrderItemResponseDTO> items,
    Instant createdAt
) {

}

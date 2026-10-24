package io.github.guzillo.fruitifystorebackend.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
    Long productId,
    String productName,
    BigDecimal priceAtPurchase,
    Integer quantity
) {

}

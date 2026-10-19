package io.github.guzillo.fruitifystorebackend.cart.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO(
    Long id,
    Long productId,
    String productName,
    BigDecimal price,
    Integer quantity
) {

}

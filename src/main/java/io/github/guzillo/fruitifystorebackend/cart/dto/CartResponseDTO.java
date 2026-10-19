package io.github.guzillo.fruitifystorebackend.cart.dto;

import java.util.List;

public record CartResponseDTO(
    Long id,
    List<CartItemResponseDTO> items
) {

}

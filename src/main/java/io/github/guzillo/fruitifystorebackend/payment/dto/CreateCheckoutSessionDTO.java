package io.github.guzillo.fruitifystorebackend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for {@link io.github.guzillo.fruitifystorebackend.order.model.Order}
 */
public record CreateCheckoutSessionDTO(@NotBlank String orderNumber) implements Serializable {

}
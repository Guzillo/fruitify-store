package io.github.guzillo.fruitifystorebackend.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for {@link io.github.guzillo.fruitifystorebackend.order.model.Order}
 */
public record CreateCheckoutSessionDTO(
    @Schema(description = "Numeric order id", example = "1")
    @NotBlank String orderNumber) implements Serializable {

}
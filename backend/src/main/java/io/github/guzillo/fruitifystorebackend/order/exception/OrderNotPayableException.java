package io.github.guzillo.fruitifystorebackend.order.exception;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;

public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(Long orderId, OrderStatus status) {
        super("Order " + orderId + " cannot be paid because its status is " + status);
    }
}

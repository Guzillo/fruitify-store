package io.github.guzillo.fruitifystorebackend.order.exception;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Order status cannot change from " + from + " to " + to);
    }
}

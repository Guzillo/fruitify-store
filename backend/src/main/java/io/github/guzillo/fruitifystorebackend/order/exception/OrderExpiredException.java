package io.github.guzillo.fruitifystorebackend.order.exception;

public class OrderExpiredException extends RuntimeException {

    public OrderExpiredException(Long orderId) {
        super("Order " + orderId + " has expired and can no longer be paid");
    }
}

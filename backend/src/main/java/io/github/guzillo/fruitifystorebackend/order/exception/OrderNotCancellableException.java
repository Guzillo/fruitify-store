package io.github.guzillo.fruitifystorebackend.order.exception;

public class OrderNotCancellableException extends RuntimeException {

    public OrderNotCancellableException(Long orderId, String reason) {
        super("Order " + orderId + " cannot be cancelled: " + reason);
    }
}

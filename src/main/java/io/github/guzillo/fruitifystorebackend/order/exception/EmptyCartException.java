package io.github.guzillo.fruitifystorebackend.order.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cart is empty");
    }
}

package io.github.guzillo.fruitifystorebackend.cart.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, Integer stockQuantity) {
        super("Insufficient stock for product " + productId + ", available: " + stockQuantity);
    }
}

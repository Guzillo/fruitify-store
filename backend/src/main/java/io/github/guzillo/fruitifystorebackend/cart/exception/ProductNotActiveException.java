package io.github.guzillo.fruitifystorebackend.cart.exception;

public class ProductNotActiveException extends RuntimeException {

    public ProductNotActiveException(Long productId) {
        super("Product is not active: " + productId);
    }
}

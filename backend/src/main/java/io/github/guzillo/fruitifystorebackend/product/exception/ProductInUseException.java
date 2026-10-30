package io.github.guzillo.fruitifystorebackend.product.exception;

public class ProductInUseException extends RuntimeException {

    public ProductInUseException(Long productId) {
        super("Product " + productId + " is part of existing orders and cannot be deleted");
    }
}

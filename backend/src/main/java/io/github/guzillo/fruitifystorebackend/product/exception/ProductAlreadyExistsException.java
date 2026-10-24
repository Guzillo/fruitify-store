package io.github.guzillo.fruitifystorebackend.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String name) {
        super("Product already exists: " + name);
    }
}

package io.github.guzillo.fruitifystorebackend.common.exception;

public class InvalidSortPropertyException extends RuntimeException {

    public InvalidSortPropertyException(String property) {
        super("Unsupported sort property: " + property);
    }
}

package io.github.guzillo.fruitifystorebackend.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " was not found: " + identifier);
    }
}

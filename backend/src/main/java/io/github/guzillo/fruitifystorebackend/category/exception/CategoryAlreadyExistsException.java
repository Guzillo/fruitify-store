package io.github.guzillo.fruitifystorebackend.category.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String name) {
        super("Category already exists: " + name);
    }
}

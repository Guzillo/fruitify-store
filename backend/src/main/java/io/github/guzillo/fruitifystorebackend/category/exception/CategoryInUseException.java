package io.github.guzillo.fruitifystorebackend.category.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(Long id) {
        super("Category is still used by products: " + id);
    }
}

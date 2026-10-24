package io.github.guzillo.fruitifystorebackend.auth.exception;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException() {
        super("Invalid token");
    }
}

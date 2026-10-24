package io.github.guzillo.fruitifystorebackend.user.exception;

public class AuthenticatedUserNotFoundException extends RuntimeException{

    public AuthenticatedUserNotFoundException(String email) {
        super("Authenticated user was not found: " + email);
    }
}

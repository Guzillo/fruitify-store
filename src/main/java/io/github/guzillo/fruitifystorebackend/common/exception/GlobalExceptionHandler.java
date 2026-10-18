package io.github.guzillo.fruitifystorebackend.common.exception;

import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.auth.exception.InvalidTokenException;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Object> handleEmailAlreadyRegisteredException(
        EmailAlreadyRegisteredException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);

    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Object> handleProductAlreadyExistsException(
        ProductAlreadyExistsException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException ex,
        WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    public ResponseEntity<Object> handleAuthenticatedUserNotFoundException(
        AuthenticatedUserNotFoundException ex, WebRequest request
    ) {
        return super.handleExceptionInternal(
            ex,
            "Authentication is no longer valid",
            new HttpHeaders(),
            HttpStatus.UNAUTHORIZED,
            request
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
        ResourceNotFoundException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}

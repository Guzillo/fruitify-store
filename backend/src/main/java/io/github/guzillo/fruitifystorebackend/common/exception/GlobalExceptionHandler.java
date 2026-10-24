package io.github.guzillo.fruitifystorebackend.common.exception;

import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.auth.exception.InvalidTokenException;
import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.cart.exception.ProductNotActiveException;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.order.exception.EmptyCartException;
import io.github.guzillo.fruitifystorebackend.order.exception.InvalidOrderStatusTransitionException;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotPayableException;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryInUseException;
import io.github.guzillo.fruitifystorebackend.payment.error.CheckoutSessionCreationException;
import io.github.guzillo.fruitifystorebackend.payment.error.WebhookSignatureException;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.common.exception.InvalidSortPropertyException;
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
    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<Object> handleCategoryAlreadyExistsException(
        CategoryAlreadyExistsException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<Object> handleCategoryInUseException(
        CategoryInUseException ex, WebRequest request) {
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

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ResponseEntity<Object> handleInvalidOrderStatusTransitionException(
        InvalidOrderStatusTransitionException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OrderNotPayableException.class)
    public ResponseEntity<Object> handleOrderNotPayableException(
        OrderNotPayableException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Object> handleEmptyCartException(
        EmptyCartException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStockException(
        InsufficientStockException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ProductNotActiveException.class)
    public ResponseEntity<Object> handleProductNotActiveException(
        ProductNotActiveException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSortPropertyException.class)
    public ResponseEntity<Object> handleInvalidSortPropertyException(
        InvalidSortPropertyException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(),
            new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
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

    @ExceptionHandler(CheckoutSessionCreationException.class)
    public ResponseEntity<Object> handleCheckoutSessionCreationException(
        CheckoutSessionCreationException ex, WebRequest request
    ) {
        return super.handleExceptionInternal(
            ex,
            "Payment provider is currently unavailable",
            new HttpHeaders(),
            HttpStatus.SERVICE_UNAVAILABLE,
            request
        );
    }

    @ExceptionHandler(WebhookSignatureException.class)
    public ResponseEntity<Object> handleWebhookSignatureException(
        WebhookSignatureException ex, WebRequest request
    ) {
        return super.handleExceptionInternal(
            ex,
            "Your signature for this webhook is invalid",
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }
}

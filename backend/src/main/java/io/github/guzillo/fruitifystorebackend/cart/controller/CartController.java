package io.github.guzillo.fruitifystorebackend.cart.controller;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "Shopping cart of the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart")
    @GetMapping
    public CartResponseDTO getCart(Authentication authentication) {
        return cartService.findCart(authentication.getName());
    }

    @Operation(summary = "Add product to cart")
    @ApiResponse(responseCode = "201", description = "Item added")
    @ApiResponse(responseCode = "409", description = "Insufficient stock or inactive product")
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponseDTO addItem(@Valid @RequestBody CartItemCreateRequestDTO request,
        Authentication authentication) {
        return cartService.addItem(authentication.getName(), request);
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/items/{itemId}")
    public CartItemResponseDTO updateItemQuantity(@PathVariable Long itemId,
        @Valid @RequestBody CartItemUpdateRequestDTO request,
        Authentication authentication) {
        return cartService.updateItemQuantity(authentication.getName(), itemId, request);
    }

    @Operation(summary = "Remove cart item")
    @ApiResponse(responseCode = "204", description = "Item removed")
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId, Authentication authentication) {
        cartService.deleteItem(authentication.getName(), itemId);
    }

    @Operation(summary = "Clear cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
    }
}

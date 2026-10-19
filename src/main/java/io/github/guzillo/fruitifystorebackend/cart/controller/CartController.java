package io.github.guzillo.fruitifystorebackend.cart.controller;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponseDTO getCart(Authentication authentication) {
        return cartService.findCart(authentication.getName());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponseDTO addItem(@Valid @RequestBody CartItemCreateRequestDTO request,
        Authentication authentication) {
        return cartService.addItem(authentication.getName(), request);
    }
}

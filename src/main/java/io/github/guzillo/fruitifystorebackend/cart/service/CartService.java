package io.github.guzillo.fruitifystorebackend.cart.service;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.exception.ProductNotActiveException;
import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.model.CartItem;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartItemRepository;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItemResponseDTO addItem(String email, CartItemCreateRequestDTO request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotActiveException(product.getId());
        }

        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseGet(() -> createCart(user));
        CartItem cartItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(() -> createCartItem(cart, product));
        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());

        CartItem saved = cartItemRepository.save(cartItem);
        return new CartItemResponseDTO(
            saved.getId(),
            product.getId(),
            product.getName(),
            product.getPrice(),
            saved.getQuantity());
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Cart cart, Product product) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(0);
        return cartItem;
    }
}

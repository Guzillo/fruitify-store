package io.github.guzillo.fruitifystorebackend.cart.service;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
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
import java.util.Comparator;
import java.util.List;
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

    @Transactional(readOnly = true)
    public CartResponseDTO findCart(String email) {
        User user = findUserByEmail(email);
        return cartRepository.findWithItemsByUserId(user.getId())
            .map(cart -> new CartResponseDTO(cart.getId(), cart.getCartItems().stream()
                .sorted(Comparator.comparing(CartItem::getId))
                .map(this::toItemResponse)
                .toList()))
            .orElseGet(() -> new CartResponseDTO(null, List.of()));
    }

    @Transactional
    public CartItemResponseDTO addItem(String email, CartItemCreateRequestDTO request) {
        User user = findUserByEmail(email);
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
        int newQuantity = cartItem.getQuantity() + request.quantity();
        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), product.getStockQuantity());
        }
        cartItem.setQuantity(newQuantity);

        return toItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponseDTO updateItemQuantity(String email, Long itemId,
        CartItemUpdateRequestDTO request) {
        User user = findUserByEmail(email);
        CartItem cartItem = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        Product product = cartItem.getProduct();
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotActiveException(product.getId());
        }
        if (request.quantity() > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), product.getStockQuantity());
        }
        cartItem.setQuantity(request.quantity());

        return toItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void deleteItem(String email, Long itemId) {
        User user = findUserByEmail(email);
        CartItem cartItem = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        cartItemRepository.delete(cartItem);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
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

    private CartItemResponseDTO toItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return new CartItemResponseDTO(
            cartItem.getId(),
            product.getId(),
            product.getName(),
            product.getPrice(),
            cartItem.getQuantity());
    }
}

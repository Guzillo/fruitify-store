package io.github.guzillo.fruitifystorebackend.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemUpdateRequestDTO;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product activeProduct;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(EMAIL);
        setId(user, 1L);

        cart = new Cart();
        cart.setUser(user);
        setId(cart, 10L);

        activeProduct = new Product();
        activeProduct.setName("Apple");
        activeProduct.setPrice(new BigDecimal("2.49"));
        activeProduct.setIsActive(true);
        activeProduct.setStockQuantity(25);
        setId(activeProduct, 100L);
    }

    @Test
    void shouldCreateCartAndAddNewItemWhenUserHasNoCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            setId(saved, 10L);
            return saved;
        });
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            setId(saved, 50L);
            return saved;
        });

        CartItemResponseDTO response = cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 3));

        assertThat(response).isEqualTo(
            new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 3));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldAddNewItemToExistingCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            setId(saved, 50L);
            return saved;
        });

        CartItemResponseDTO response = cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 2));

        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.productId()).isEqualTo(100L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldAddQuantityToExistingCartItem() {
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(activeProduct);
        existing.setQuantity(4);
        setId(existing, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponseDTO response = cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 3));

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.quantity()).isEqualTo(7);
        assertThat(existing.getQuantity()).isEqualTo(7);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(999L, 1)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenProductIsInactive() {
        activeProduct.setIsActive(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 1)))
            .isInstanceOf(ProductNotActiveException.class);

        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 1)))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(productRepository, never()).findById(any());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldReturnCartWithItemsForUser() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(activeProduct);
        item.setQuantity(3);
        setId(item, 50L);
        cart.getCartItems().add(item);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(1L)).thenReturn(Optional.of(cart));

        CartResponseDTO response = cartService.findCart(EMAIL);

        assertThat(response).isEqualTo(new CartResponseDTO(10L, List.of(
            new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 3))));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNoCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(1L)).thenReturn(Optional.empty());

        CartResponseDTO response = cartService.findCart(EMAIL);

        assertThat(response).isEqualTo(new CartResponseDTO(null, List.of()));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExistOnFindCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.findCart(EMAIL))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(cartRepository, never()).findWithItemsByUserId(any());
    }

    @Test
    void shouldUpdateQuantityOfOwnCartItem() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(activeProduct);
        item.setQuantity(3);
        setId(item, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(item)).thenReturn(item);

        CartItemResponseDTO response = cartService.updateItemQuantity(EMAIL, 50L,
            new CartItemUpdateRequestDTO(5));

        assertThat(response).isEqualTo(
            new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 5));
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenProductInCartIsInactiveOnUpdate() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(activeProduct);
        item.setQuantity(3);
        setId(item, 50L);
        activeProduct.setIsActive(false);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateItemQuantity(EMAIL, 50L,
            new CartItemUpdateRequestDTO(5)))
            .isInstanceOf(ProductNotActiveException.class);

        assertThat(item.getQuantity()).isEqualTo(3);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldAllowAddingUpToExactStock() {
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(activeProduct);
        existing.setQuantity(20);
        setId(existing, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponseDTO response = cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 5));

        assertThat(response.quantity()).isEqualTo(25);
    }

    @Test
    void shouldThrowWhenAddingMoreThanStockForNewItem() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 26)))
            .isInstanceOf(InsufficientStockException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenMergedQuantityExceedsStock() {
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(activeProduct);
        existing.setQuantity(20);
        setId(existing, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> cartService.addItem(EMAIL,
            new CartItemCreateRequestDTO(100L, 6)))
            .isInstanceOf(InsufficientStockException.class);

        assertThat(existing.getQuantity()).isEqualTo(20);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenUpdatedQuantityExceedsStock() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(activeProduct);
        item.setQuantity(3);
        setId(item, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateItemQuantity(EMAIL, 50L,
            new CartItemUpdateRequestDTO(26)))
            .isInstanceOf(InsufficientStockException.class);

        assertThat(item.getQuantity()).isEqualTo(3);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldDeleteOwnCartItem() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(activeProduct);
        item.setQuantity(3);
        setId(item, 50L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.of(item));

        cartService.deleteItem(EMAIL, 50L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void shouldThrowWhenDeletingCartItemThatDoesNotBelongToUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteItem(EMAIL, 50L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CartItem");

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExistOnDelete() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteItem(EMAIL, 50L))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(cartItemRepository, never()).findByIdAndCartUserId(any(), any());
    }

    @Test
    void shouldThrowWhenCartItemDoesNotBelongToUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByIdAndCartUserId(50L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity(EMAIL, 50L,
            new CartItemUpdateRequestDTO(5)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CartItem");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExistOnUpdate() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity(EMAIL, 50L,
            new CartItemUpdateRequestDTO(5)))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(cartItemRepository, never()).findByIdAndCartUserId(any(), any());
    }

    private static void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}

package io.github.guzillo.fruitifystorebackend.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.dto.CartResponseDTO;
import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.cart.exception.ProductNotActiveException;
import io.github.guzillo.fruitifystorebackend.cart.service.CartService;
import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.security.config.SecurityConfig;
import io.github.guzillo.fruitifystorebackend.security.service.CustomUserDetailsService;
import java.math.BigDecimal;
import java.util.List;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CartController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CartControllerTest {

    private static final String VALID_BODY = """
        {"productId": 100, "quantity": 2}
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private RSAPublicKey jwtPublicKey;

    @MockitoBean
    private RSAPrivateKey jwtPrivateKey;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @Test
    void shouldAddItemToCartForAuthenticatedUser() throws Exception {
        when(cartService.addItem(any(), any())).thenReturn(
            new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 2));

        mockMvc.perform(post("/api/cart/items")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(50))
            .andExpect(jsonPath("$.productId").value(100))
            .andExpect(jsonPath("$.productName").value("Apple"))
            .andExpect(jsonPath("$.price").value(2.49))
            .andExpect(jsonPath("$.quantity").value(2));

        verify(cartService).addItem("user@example.com",
            new CartItemCreateRequestDTO(
                100L, 2));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectMissingProductId() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 2}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectNonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": 100, "quantity": 0}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(cartService.addItem(any(), any()))
            .thenThrow(new ResourceNotFoundException("Product", 100L));

        mockMvc.perform(post("/api/cart/items")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenProductIsInactive() throws Exception {
        when(cartService.addItem(any(), any()))
            .thenThrow(new ProductNotActiveException(100L));

        mockMvc.perform(post("/api/cart/items")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnCartForAuthenticatedUser() throws Exception {
        when(cartService.findCart("user@example.com")).thenReturn(new CartResponseDTO(10L,
            List.of(new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 3))));

        mockMvc.perform(get("/api/cart")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com"))))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.items[0].productId").value(100))
            .andExpect(jsonPath("$.items[0].productName").value("Apple"))
            .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void shouldRejectUnauthenticatedGetCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldUpdateCartItemQuantityForAuthenticatedUser() throws Exception {
        when(cartService.updateItemQuantity(any(), any(), any())).thenReturn(
            new CartItemResponseDTO(50L, 100L, "Apple", new BigDecimal("2.49"), 5));

        mockMvc.perform(put("/api/cart/items/50")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 5}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(50))
            .andExpect(jsonPath("$.quantity").value(5));

        verify(cartService).updateItemQuantity("user@example.com", 50L,
            new CartItemUpdateRequestDTO(5));
    }

    @Test
    void shouldRejectUpdateWithNonPositiveQuantity() throws Exception {
        mockMvc.perform(put("/api/cart/items/50")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 0}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnNotFoundWhenCartItemDoesNotExist() throws Exception {
        when(cartService.updateItemQuantity(any(), any(), any()))
            .thenThrow(new ResourceNotFoundException("CartItem", 50L));

        mockMvc.perform(put("/api/cart/items/50")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 5}
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUnauthenticatedUpdate() throws Exception {
        mockMvc.perform(put("/api/cart/items/50")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 5}
                    """))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnConflictWhenQuantityExceedsStock() throws Exception {
        when(cartService.addItem(any(), any()))
            .thenThrow(new InsufficientStockException(100L, 25));

        mockMvc.perform(post("/api/cart/items")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteCartItemForAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/cart/items/50")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com"))))
            .andExpect(status().isNoContent());

        verify(cartService).deleteItem("user@example.com", 50L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingCartItem() throws Exception {
        doThrow(new ResourceNotFoundException("CartItem", 50L))
            .when(cartService).deleteItem(any(), any());

        mockMvc.perform(delete("/api/cart/items/50")
                .with(jwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/cart/items/50"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldClearCartForAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/cart")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com"))))
            .andExpect(status().isNoContent());

        verify(cartService).clearCart("user@example.com");
    }

    @Test
    void shouldRejectUnauthenticatedClearCart() throws Exception {
        mockMvc.perform(delete("/api/cart"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }
}

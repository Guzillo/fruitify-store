package io.github.guzillo.fruitifystorebackend.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.ShippingAddressDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.EmptyCartException;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.github.guzillo.fruitifystorebackend.security.config.RsaKeyProperties;
import io.github.guzillo.fruitifystorebackend.security.config.SecurityConfig;
import io.github.guzillo.fruitifystorebackend.security.service.CustomUserDetailsService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    private static final String VALID_BODY = """
        {"shippingAddress": {"recipientName": "Jan Kowalski", "street": "Marszałkowska 1",
          "city": "Warsaw", "postalCode": "00-001", "country": "PL"}}
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private RsaKeyProperties rsaKeyProperties;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @Test
    void shouldCreateOrderForAuthenticatedUser() throws Exception {
        when(orderService.createOrder(any(), any())).thenReturn(new OrderResponseDTO(
            200L, "ORD-0123456789AB", OrderStatus.PENDING, new BigDecimal("7.47"),
            new ShippingAddressDTO("Jan Kowalski", "Marszałkowska 1", "Warsaw", "00-001", "PL"),
            List.of(new OrderItemResponseDTO(100L, "Apple", new BigDecimal("2.49"), 3)),
            null));

        mockMvc.perform(post("/api/orders")
                .with(jwt().jwt(jwt -> jwt.subject("user@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(200))
            .andExpect(jsonPath("$.orderNumber").value("ORD-0123456789AB"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(7.47))
            .andExpect(jsonPath("$.items[0].productId").value(100))
            .andExpect(jsonPath("$.shippingAddress.city").value("Warsaw"));

        verify(orderService).createOrder(any(), any());
    }

    @Test
    void shouldRejectMissingShippingCity() throws Exception {
        mockMvc.perform(post("/api/orders")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"shippingAddress": {"recipientName": "Jan", "street": "Street 1",
                      "postalCode": "00-001", "country": "PL"}}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturnConflictWhenCartIsEmpty() throws Exception {
        when(orderService.createOrder(any(), any())).thenThrow(new EmptyCartException());

        mockMvc.perform(post("/api/orders")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        when(orderService.createOrder(any(), any()))
            .thenThrow(new InsufficientStockException(100L, 2));

        mockMvc.perform(post("/api/orders")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }
}

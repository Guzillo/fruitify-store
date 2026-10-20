package io.github.guzillo.fruitifystorebackend.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderSummaryDTO;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.github.guzillo.fruitifystorebackend.security.config.RsaKeyProperties;
import io.github.guzillo.fruitifystorebackend.security.config.SecurityConfig;
import io.github.guzillo.fruitifystorebackend.security.service.CustomUserDetailsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminOrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminOrderControllerTest {

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
    void shouldListAllOrdersForAdmin() throws Exception {
        when(orderService.findAllOrders(any())).thenReturn(new AdminOrderPageResponseDTO(
            List.of(new AdminOrderSummaryDTO(200L, "ORD-0123456789AB", OrderStatus.PAID,
                new BigDecimal("7.47"), Instant.parse("2026-10-20T10:00:00Z"), 1L,
                "customer@example.com")),
            0, 20, 1, 1));

        mockMvc.perform(get("/api/admin/orders")
                .with(jwt().jwt(jwt -> jwt.subject("admin@example.com"))
                    .authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].customerEmail").value("customer@example.com"))
            .andExpect(jsonPath("$.content[0].userId").value(1))
            .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).findAllOrders(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending())
            .isTrue();
    }

    @Test
    void shouldRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                .with(jwt().authorities(() -> "ROLE_USER")))
            .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectUnsupportedSortProperty() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                .with(jwt().authorities(() -> "ROLE_ADMIN"))
                .queryParam("sort", "user.password,asc"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}

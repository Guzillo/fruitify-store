package io.github.guzillo.fruitifystorebackend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotCancellableException;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCancellationServiceTest {

    private static final String EMAIL = "customer@example.com";

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderCancellationService cancellationService;

    @Test
    void shouldCancelPendingOrderWithoutStripeSessionDirectly() {
        when(orderService.findOrder(EMAIL, 1L)).thenReturn(order(OrderStatus.PENDING));
        when(orderService.findCheckoutSessionId(1L)).thenReturn(null);
        when(orderService.cancelPendingOrder(1L)).thenReturn(order(OrderStatus.CANCELLED));

        OrderResponseDTO result = cancellationService.cancel(EMAIL, 1L);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(paymentService, never()).closeCheckoutSession(any());
    }

    @Test
    void shouldCloseOpenStripeSessionBeforeCancelling() {
        when(orderService.findOrder(EMAIL, 1L)).thenReturn(order(OrderStatus.PENDING));
        when(orderService.findCheckoutSessionId(1L)).thenReturn("cs_open");
        when(paymentService.closeCheckoutSession("cs_open")).thenReturn(true);
        when(orderService.cancelPendingOrder(1L)).thenReturn(order(OrderStatus.CANCELLED));

        cancellationService.cancel(EMAIL, 1L);

        InOrder order = inOrder(paymentService, orderService);
        order.verify(paymentService).closeCheckoutSession("cs_open");
        order.verify(orderService).cancelPendingOrder(1L);
    }

    @Test
    void shouldRefuseToCancelWhenStripeSessionAlreadyPaid() {
        when(orderService.findOrder(EMAIL, 1L)).thenReturn(order(OrderStatus.PENDING));
        when(orderService.findCheckoutSessionId(1L)).thenReturn("cs_paid");
        when(paymentService.closeCheckoutSession("cs_paid")).thenReturn(false);

        assertThatThrownBy(() -> cancellationService.cancel(EMAIL, 1L))
            .isInstanceOf(OrderNotCancellableException.class);

        verify(orderService, never()).cancelPendingOrder(any());
    }

    @Test
    void shouldRefuseToCancelPaidOrder() {
        when(orderService.findOrder(EMAIL, 1L)).thenReturn(order(OrderStatus.PAID));

        assertThatThrownBy(() -> cancellationService.cancel(EMAIL, 1L))
            .isInstanceOf(OrderNotCancellableException.class);

        verify(orderService, never()).cancelPendingOrder(any());
        verify(paymentService, never()).closeCheckoutSession(any());
    }

    @Test
    void shouldReturnAlreadyCancelledOrderUnchanged() {
        when(orderService.findOrder(EMAIL, 1L)).thenReturn(order(OrderStatus.CANCELLED));

        assertThat(cancellationService.cancel(EMAIL, 1L).status())
            .isEqualTo(OrderStatus.CANCELLED);

        verify(orderService, never()).cancelPendingOrder(any());
    }

    private OrderResponseDTO order(OrderStatus status) {
        return new OrderResponseDTO(1L, "ORD-1", status, new BigDecimal("2.49"), null,
            List.of(), Instant.now());
    }
}

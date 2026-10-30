package io.github.guzillo.fruitifystorebackend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.V1Services;
import com.stripe.service.checkout.SessionService;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.github.guzillo.fruitifystorebackend.payment.config.StripeProperties;
import io.github.guzillo.fruitifystorebackend.payment.error.CheckoutSessionCreationException;
import io.github.guzillo.fruitifystorebackend.payment.error.WebhookSignatureException;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderExpiredException;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotPayableException;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private StripeClient stripeClient;

    @Mock
    private V1Services stripeV1Services;

    @Mock
    private com.stripe.service.CheckoutService stripeCheckoutService;

    @Mock
    private SessionService stripeSessionService;

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private OrderService orderService;

    @Mock
    private Session stripeSession;

    private PaymentService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new PaymentService(stripeClient, stripeProperties, orderService);
    }

    @Test
    void shouldCreateStripeSessionWithOrderItems() throws StripeException {
        when(stripeProperties.domain()).thenReturn("https://shop.example.com");
        when(orderService.findOrder("customer@example.com", 123L)).thenReturn(order());
        stubStripeSessionService();
        when(stripeSessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);

        Session result = checkoutService.createCheckoutSession("customer@example.com", "123");

        assertThat(result).isSameAs(stripeSession);
        verify(orderService).findOrder("customer@example.com", 123L);
        ArgumentCaptor<SessionCreateParams> paramsCaptor =
            ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue().getMetadata())
            .containsEntry("orderId", "123");
        assertThat(paramsCaptor.getValue().getCancelUrl())
            .isEqualTo("https://shop.example.com/orders/123?payment=cancelled");
        assertThat(paramsCaptor.getValue().getExpiresAt())
            .isGreaterThan(Instant.now().getEpochSecond() + 30 * 60L);
    }

    @Test
    void shouldRejectCheckoutSessionForOrderTooCloseToItsDeadline() {
        when(stripeProperties.domain()).thenReturn("https://shop.example.com");
        when(orderService.findOrder("customer@example.com", 123L))
            .thenReturn(orderCreatedAt(Instant.now().minusSeconds(115 * 60L)));

        assertThatThrownBy(
                () -> checkoutService.createCheckoutSession("customer@example.com", "123"))
            .isInstanceOf(OrderExpiredException.class);

        verify(stripeClient, never()).v1();
    }

    @Test
    void shouldRejectCheckoutSessionForPaidOrder() {
        when(orderService.findOrder("customer@example.com", 123L))
            .thenReturn(orderWithStatus(OrderStatus.PAID));

        assertThatThrownBy(
                () -> checkoutService.createCheckoutSession("customer@example.com", "123"))
            .isInstanceOf(OrderNotPayableException.class)
            .hasMessage("Order 123 cannot be paid because its status is PAID");

        verify(stripeClient, never()).v1();
    }

    @Test
    void shouldTranslateStripeFailureToCheckoutSessionCreationException() throws StripeException {
        when(stripeProperties.domain()).thenReturn("https://shop.example.com");
        when(orderService.findOrder("customer@example.com", 123L)).thenReturn(order());
        stubStripeSessionService();
        when(stripeSessionService.create(any(SessionCreateParams.class)))
            .thenThrow(mock(StripeException.class));

        assertThatThrownBy(() -> checkoutService.createCheckoutSession("customer@example.com", "123"))
            .isInstanceOf(CheckoutSessionCreationException.class);
    }

    @Test
    void shouldRejectWebhookWithInvalidSignature() {
        when(stripeProperties.webhookSecret()).thenReturn("whsec_test");

        assertThatThrownBy(() -> checkoutService.handleWebhookStripe(
            "{\"id\":\"evt_test\"}",
            "t=1700000000,v1=invalid"))
            .isInstanceOf(WebhookSignatureException.class);

        verify(orderService, never()).changeStatus(any(), any());
    }

    @Test
    void shouldExpireOpenStripeSession() throws StripeException {
        stubStripeSessionService();
        when(stripeSessionService.retrieve("cs_open")).thenReturn(stripeSession);
        when(stripeSession.getStatus()).thenReturn("open");

        assertThat(checkoutService.closeCheckoutSession("cs_open")).isTrue();

        verify(stripeSessionService).expire("cs_open");
    }

    @Test
    void shouldReportPaidStripeSessionWithoutExpiringIt() throws StripeException {
        stubStripeSessionService();
        when(stripeSessionService.retrieve("cs_paid")).thenReturn(stripeSession);
        when(stripeSession.getStatus()).thenReturn("complete");

        assertThat(checkoutService.closeCheckoutSession("cs_paid")).isFalse();

        verify(stripeSessionService, never()).expire(any());
    }

    @Test
    void shouldStoreStripeSessionIdOnOrder() throws StripeException {
        when(stripeProperties.domain()).thenReturn("https://shop.example.com");
        when(orderService.findOrder("customer@example.com", 123L)).thenReturn(order());
        stubStripeSessionService();
        when(stripeSessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);
        when(stripeSession.getId()).thenReturn("cs_new");

        checkoutService.createCheckoutSession("customer@example.com", "123");

        verify(orderService).attachCheckoutSession(123L, "cs_new");
    }

    private void stubStripeSessionService() {
        when(stripeClient.v1()).thenReturn(stripeV1Services);
        when(stripeV1Services.checkout()).thenReturn(stripeCheckoutService);
        when(stripeCheckoutService.sessions()).thenReturn(stripeSessionService);
    }

    private OrderResponseDTO order() {
        return orderWithStatus(OrderStatus.PENDING);
    }

    private OrderResponseDTO orderWithStatus(OrderStatus status) {
        return new OrderResponseDTO(
            123L,
            "123",
            status,
            new BigDecimal("24.98"),
            null,
            List.of(new OrderItemResponseDTO(
                10L, "Apples", new BigDecimal("2.49"), 2)),
            Instant.now());
    }

    private OrderResponseDTO orderCreatedAt(Instant createdAt) {
        return new OrderResponseDTO(
            123L,
            "123",
            OrderStatus.PENDING,
            new BigDecimal("24.98"),
            null,
            List.of(new OrderItemResponseDTO(
                10L, "Apples", new BigDecimal("2.49"), 2)),
            createdAt);
    }
}

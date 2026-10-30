package io.github.guzillo.fruitifystorebackend.payment.service;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderExpiredException;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotPayableException;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.github.guzillo.fruitifystorebackend.order.service.PaymentWindow;
import io.github.guzillo.fruitifystorebackend.payment.config.StripeProperties;
import io.github.guzillo.fruitifystorebackend.payment.error.CheckoutSessionCreationException;
import io.github.guzillo.fruitifystorebackend.payment.error.WebhookSignatureException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final OrderService orderService;

    public Session createCheckoutSession(String email, String orderId) {
        String domain = stripeProperties.domain();
        String successUrl = domain + "/checkout?success=true";
        String cancelUrl = domain + "/orders/" + orderId + "?payment=cancelled";

        OrderResponseDTO order = orderService.findOrder(email, Long.valueOf(orderId));
        if (order.status() != OrderStatus.PENDING) {
            throw new OrderNotPayableException(order.id(), order.status());
        }

        // The session must expire before the order is cancelled, so a payment can never
        // arrive for an order whose stock was already released.
        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plus(PaymentWindow.SESSION_LIFETIME);
        Instant orderDeadline = order.createdAt().plus(PaymentWindow.ORDER_TTL);
        if (sessionExpiresAt.plus(PaymentWindow.SAFETY_MARGIN).isAfter(orderDeadline)) {
            throw new OrderExpiredException(order.id());
        }

        // Close the previous attempt so it cannot be paid after this one (or after a cancel).
        String previousSessionId = orderService.findCheckoutSessionId(order.id());
        if (previousSessionId != null && !closeCheckoutSession(previousSessionId)) {
            throw new OrderNotPayableException(order.id(), order.status());
        }

        List<OrderItemResponseDTO> orderItems = order.items();
        List<LineItem> lineItems = toLineItems(orderItems);

        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setExpiresAt(sessionExpiresAt.getEpochSecond())
                .putMetadata("orderId", order.id().toString())
                .addAllLineItem(lineItems)
                .build();
        try {
            Session session = stripeClient.v1().checkout().sessions().create(params);
            orderService.attachCheckoutSession(order.id(), session.getId());
            return session;
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new CheckoutSessionCreationException();
        }
    }

    /**
     * Makes sure a Stripe session can no longer be paid.
     *
     * @return false if the session was already paid, true if it is now closed
     */
    public boolean closeCheckoutSession(String sessionId) {
        try {
            Session session = stripeClient.v1().checkout().sessions().retrieve(sessionId);
            if ("complete".equals(session.getStatus())) {
                return false;
            }
            if ("open".equals(session.getStatus())) {
                stripeClient.v1().checkout().sessions().expire(sessionId);
            }
            return true;
        } catch (StripeException e) {
            log.error("Could not close Stripe session {}: {}", sessionId, e.getMessage());
            throw new CheckoutSessionCreationException();
        }
    }

    public void handleWebhookStripe(String payload, String sigHeader) {
        String webhookSecret = stripeProperties.webhookSecret();

        try {
            Event event = Webhook.constructEvent(
                payload, sigHeader, webhookSecret
            );

            if (!"checkout.session.completed".equals(event.getType())) {
                return;
            }

            String orderId = getOrderId(event);

            orderService.changeStatus(Long.valueOf(orderId), OrderStatus.PAID);
            log.info("Marked order {} as paid", orderId);
        } catch (SignatureVerificationException e) {
            throw new WebhookSignatureException();
        }
    }

    private static @NonNull String getOrderId(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject()
            .orElseThrow(() -> new IllegalStateException(
                "Stripe checkout session could not be deserialized"));
        Map<String, String> metadata = session.getMetadata();
        String orderId = metadata == null ? null : metadata.get("orderId");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalStateException(
                "Stripe checkout session is missing orderId metadata");
        }
        return orderId;
    }

    private List<LineItem> toLineItems(List<OrderItemResponseDTO> orderItems) {
        return orderItems.stream()
            .map(item -> SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.quantity()))
                .setPriceData(
                    PriceData.builder()
                        .setCurrency("pln")
                        .setUnitAmount(toUnitAmount(item.priceAtPurchase()))
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.productName())
                                .build()
                        )
                        .build()
                )
                .build())
            .toList();
    }

    private Long toUnitAmount(BigDecimal price) {
        return price
            .movePointRight(2)
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact();
    }


}
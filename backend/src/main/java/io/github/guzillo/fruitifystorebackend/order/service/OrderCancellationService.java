package io.github.guzillo.fruitifystorebackend.order.service;

import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotCancellableException;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * Cancels the user's own unpaid order: closes its Stripe session so it can no longer be
     * paid, then releases the stock. Paid orders are refused.
     */
    public OrderResponseDTO cancel(String email, Long orderId) {
        OrderResponseDTO order = orderService.findOrder(email, orderId);
        if (order.status() == OrderStatus.CANCELLED) {
            return order;
        }
        if (order.status() != OrderStatus.PENDING) {
            throw new OrderNotCancellableException(orderId,
                "its status is " + order.status());
        }

        String sessionId = orderService.findCheckoutSessionId(orderId);
        if (sessionId != null && !paymentService.closeCheckoutSession(sessionId)) {
            throw new OrderNotCancellableException(orderId, "a payment has already been made");
        }

        return orderService.cancelPendingOrder(orderId);
    }
}

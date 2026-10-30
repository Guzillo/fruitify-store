package io.github.guzillo.fruitifystorebackend.order.service;

import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Releases stock of unpaid orders once their payment window has closed.
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiryJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelayString = "PT1M")
    public void cancelExpiredOrders() {
        Instant now = Instant.now();
        List<Long> candidates = orderRepository.findIdsByStatusAndCreatedAtBefore(
            OrderStatus.PENDING, now.minus(PaymentWindow.ORDER_TTL));

        for (Long orderId : candidates) {
            try {
                if (orderService.cancelIfExpired(orderId, now)) {
                    log.info("Cancelled unpaid order {} and released its stock", orderId);
                }
            } catch (RuntimeException e) {
                // One bad order must not stop the others from being processed.
                log.error("Could not expire order {}", orderId, e);
            }
        }
    }
}

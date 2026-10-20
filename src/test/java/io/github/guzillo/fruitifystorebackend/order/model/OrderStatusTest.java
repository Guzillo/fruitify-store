package io.github.guzillo.fruitifystorebackend.order.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void shouldAllowPendingToBecomePaidOrCancelled() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void shouldAllowPaidToBecomeCancelled() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void shouldRejectOtherTransitionsIncludingSameStatus() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void shouldTreatCancelledAsTerminal() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }
}

package io.github.guzillo.fruitifystorebackend.order.service;

import java.time.Duration;

/**
 * Timing rules for paying orders.
 *
 * <p>Invariant: a Stripe session created for an order always expires before the order
 * is cancelled. Otherwise a customer could pay for an order whose stock was already released.
 */
public final class PaymentWindow {

    /** Lifetime of a Stripe Checkout session. Stripe requires at least 30 minutes. */
    public static final Duration SESSION_LIFETIME = Duration.ofMinutes(31);

    /** Unpaid orders are cancelled and their stock released this long after creation. */
    public static final Duration ORDER_TTL = Duration.ofHours(2);

    /** Extra time a session must leave before the order deadline. */
    public static final Duration SAFETY_MARGIN = Duration.ofMinutes(5);

    private PaymentWindow() {
    }
}

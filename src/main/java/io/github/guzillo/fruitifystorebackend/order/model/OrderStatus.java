package io.github.guzillo.fruitifystorebackend.order.model;

public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED;
            case PAID -> target == CANCELLED;
            case CANCELLED -> false;
        };
    }
}

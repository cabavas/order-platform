package com.github.cabavas.orderplatform.orders.order.domain;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAID,
    CANCELLED,
    CONFIRMED,
    FAILED
}

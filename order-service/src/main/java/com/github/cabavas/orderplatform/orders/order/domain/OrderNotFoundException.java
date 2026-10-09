package com.github.cabavas.orderplatform.orders.order.domain;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(UUID id) {
        super("Order not found: " + id);
    }
}

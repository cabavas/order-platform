package com.github.cabavas.orderplatform.contracts.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        int schemaVersion,
        UUID eventId,
        Instant ocurredAt,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency,
        List<OrderItemPayload> items
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public static OrderCreatedEvent of(
            UUID eventId,
            UUID orderId,
            UUID customerId,
            BigDecimal totalAmount,
            String currency,
            List<OrderItemPayload> items
    ) {
        return new OrderCreatedEvent(
                CURRENT_SCHEMA_VERSION,
                eventId,
                Instant.now(),
                orderId,
                customerId,
                totalAmount,
                currency,
                List.copyOf(items)
        );
    }
}

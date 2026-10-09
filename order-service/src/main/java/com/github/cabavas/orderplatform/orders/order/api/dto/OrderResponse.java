package com.github.cabavas.orderplatform.orders.order.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        List<OrderItemResponse> items
) {
}

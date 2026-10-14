package com.github.cabavas.orderplatform.contracts.order;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot of an order item at the moment the order was created.
 * Prices are immutable historical data: later changes to the product
 * catalog must not affect past orders.
 */
public record OrderItemPayload(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
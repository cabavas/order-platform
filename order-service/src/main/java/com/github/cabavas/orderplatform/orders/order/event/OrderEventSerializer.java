package com.github.cabavas.orderplatform.orders.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cabavas.orderplatform.contracts.order.OrderCreatedEvent;
import com.github.cabavas.orderplatform.contracts.order.OrderItemPayload;
import com.github.cabavas.orderplatform.orders.order.domain.Order;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class OrderEventSerializer {

    private final ObjectMapper objectMapper;

    public OrderEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SerializedEvent serializeOrderCreated(Order order) {
        UUID eventId = UUID.randomUUID();
        var payload = new OrderCreatedEvent(
                OrderCreatedEvent.CURRENT_SCHEMA_VERSION,
                eventId,
                order.getCreatedAt(),
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getItems().stream()
                        .map(item -> new OrderItemPayload(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()))
                        .toList()
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            return new SerializedEvent(eventId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OrderCreatedEvent", e);
        }
    }

    public record SerializedEvent(UUID eventId, String json) {
    }
}
package com.github.cabavas.orderplatform.orders.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(UUID customerId, String currency) {
        if(customerId == null) throw new IllegalArgumentException("CustomerId can't be null");
        Order order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.currency = currency != null ? currency : "BRL";
        order.totalAmount = BigDecimal.ZERO;
        return order;
    }

    public void addItem(UUID productId, String productName, int quantity, BigDecimal unitPrice) {
        if(status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Can't change items of an order in " + status);
        }
        OrderItem item = OrderItem.create(this, productId, productName, quantity, unitPrice);
        items.add(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAsPaymentProcessing() {
        transitionTo(OrderStatus.PAYMENT_PROCESSING);
    }

    public void markAsPaid() {
        transitionTo(OrderStatus.PAID);
    }

    public void markAsConfirmed() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    public void fail() {
        transitionTo(OrderStatus.FAILED);
    }

    private void transitionTo(OrderStatus next) {
        if (!isValidTransition(this.status, next)) {
            throw new InvalidOrderStateException("Transição inválida: " + status + " -> " + next);
        }
        this.status = next;
    }

    private static boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING            -> to == OrderStatus.PAYMENT_PROCESSING || to == OrderStatus.CANCELLED;
            case PAYMENT_PROCESSING -> to == OrderStatus.PAID || to == OrderStatus.FAILED || to == OrderStatus.CANCELLED;
            case PAID               -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED          -> to == OrderStatus.CANCELLED;
            case CANCELLED, FAILED  -> false;
        };
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}

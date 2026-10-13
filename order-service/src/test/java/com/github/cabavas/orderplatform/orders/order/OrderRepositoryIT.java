package com.github.cabavas.orderplatform.orders.order;

import com.github.cabavas.orderplatform.orders.AbstractIntegrationTest;
import com.github.cabavas.orderplatform.orders.order.domain.InvalidOrderStateException;
import com.github.cabavas.orderplatform.orders.order.domain.Order;
import com.github.cabavas.orderplatform.orders.order.domain.OrderStatus;
import com.github.cabavas.orderplatform.orders.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class OrderRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveOrderWithItemsAndCalculateTotal() {
        UUID customerId = UUID.randomUUID();
        Order order = Order.create(customerId, "BRL");
        order.addItem(UUID.randomUUID(), "Mechanical Keyboard", 1, new BigDecimal("450.00"));
        order.addItem(UUID.randomUUID(), "Gaming Mouse", 2, new BigDecimal("150.00"));

        Order saved = orderRepository.saveAndFlush(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("750.00");
        assertThat(saved.getItems()).hasSize(2);

        Order reloaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getItems()).hasSize(2);
        assertThat(reloaded.getTotalAmount()).isEqualByComparingTo("750.00");
    }

    @Test
    void shouldNotAllowInvalidStateTransition() {
        Order order = Order.create(UUID.randomUUID(), "BRL");
        order.addItem(UUID.randomUUID(), "Monitor", 1, new BigDecimal("1200.00"));
        order.cancel();

        assertThatThrownBy(order::markAsPaid)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Invalid transition");
    }
}

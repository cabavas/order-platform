package com.github.cabavas.orderplatform.orders.order.service;

import com.github.cabavas.orderplatform.orders.order.api.dto.CreateOrderRequest;
import com.github.cabavas.orderplatform.orders.order.api.dto.OrderResponse;
import com.github.cabavas.orderplatform.orders.order.domain.Order;
import com.github.cabavas.orderplatform.orders.order.domain.OrderNotFoundException;
import com.github.cabavas.orderplatform.orders.order.mapper.OrderMapper;
import com.github.cabavas.orderplatform.orders.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {
    private static final String DEFAULT_CURRENCY = "BRL";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderApplicationService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.create(request.customerId(), DEFAULT_CURRENCY);
        request.items().forEach(item ->
                order.addItem(item.productId(), item.productName(), item.quantity(), item.unitPrice())
        );
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Transactional()
    public OrderResponse findById(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomer(UUID customerId) {
        return orderMapper.toResponseList(
                orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
        );
    }
}

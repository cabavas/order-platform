package com.github.cabavas.orderplatform.orders.order.api;

import com.github.cabavas.orderplatform.orders.order.api.dto.CreateOrderRequest;
import com.github.cabavas.orderplatform.orders.order.api.dto.OrderResponse;
import com.github.cabavas.orderplatform.orders.order.service.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @GetMapping
    public List<OrderResponse> findByCustomer(@RequestParam UUID customerId) {
        return orderService.findByCustomer(customerId);
    }
}
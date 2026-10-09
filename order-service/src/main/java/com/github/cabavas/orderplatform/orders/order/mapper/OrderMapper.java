package com.github.cabavas.orderplatform.orders.order.mapper;

import com.github.cabavas.orderplatform.orders.order.api.dto.OrderItemResponse;
import com.github.cabavas.orderplatform.orders.order.api.dto.OrderResponse;
import com.github.cabavas.orderplatform.orders.order.domain.Order;
import com.github.cabavas.orderplatform.orders.order.domain.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    OrderItemResponse toItemResponse(OrderItem item);
    List<OrderResponse> toResponseList(List<Order> orders);
}

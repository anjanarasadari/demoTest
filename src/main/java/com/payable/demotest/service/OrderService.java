package com.payable.demotest.service;

import com.payable.demotest.dto.CreateOrderRequest;
import com.payable.demotest.dto.OrderResponse;
import com.payable.demotest.entity.Order;
import com.payable.demotest.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(String orderId);

    Page<OrderResponse> getOrdersByCustomerId(String customerId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus);

    void deleteOrder(String orderId);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);
}


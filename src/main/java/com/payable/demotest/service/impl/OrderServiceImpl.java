package com.payable.demotest.service.impl;

import com.payable.demotest.dto.CreateOrderRequest;
import com.payable.demotest.dto.OrderItemResponse;
import com.payable.demotest.dto.OrderResponse;
import com.payable.demotest.entity.*;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.OrderItemRepository;
import com.payable.demotest.repository.OrderRepository;
import com.payable.demotest.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.error("Order must contain at least one item");
            throw new InvalidOperationException("Order must contain at least one item");
        }

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getOrderId());

        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(savedOrder)
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(itemRequest.getUnitPrice())
                        .totalPrice(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                        .status(OrderItemStatus.RESERVED)
                        .build())
                .collect(Collectors.toList());

        List<OrderItem> savedItems = orderItemRepository.saveAll(items);
        savedOrder.setOrderItems(savedItems);

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        log.debug("Fetching order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerId(String customerId, Pageable pageable) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching all orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("Updating order status. Order ID: {}, New Status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully. Order ID: {}, New Status: {}", orderId, newStatus);
        return mapToResponse(updatedOrder);
    }

    @Override
    public void deleteOrder(String orderId) {
        log.info("Deleting order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });

        if (!order.getStatus().equals(OrderStatus.PENDING) && !order.getStatus().equals(OrderStatus.CANCELLED)) {
            log.error("Cannot delete order with status: {}", order.getStatus());
            throw new InvalidOperationException("Cannot delete order that is not in PENDING or CANCELLED status");
        }

        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully with ID: {}", orderId);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus.equals(OrderStatus.DELIVERED) || currentStatus.equals(OrderStatus.CANCELLED)) {
            log.error("Cannot transition from status: {}", currentStatus);
            throw new InvalidOperationException("Cannot transition from " + currentStatus + " status");
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getOrderItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .status(item.getStatus().toString())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().toString())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(itemResponses)
                .build();
    }
}


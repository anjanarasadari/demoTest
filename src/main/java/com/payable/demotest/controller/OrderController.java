package com.payable.demotest.controller;

import com.payable.demotest.dto.CreateOrderRequest;
import com.payable.demotest.dto.OrderResponse;
import com.payable.demotest.entity.OrderStatus;
import com.payable.demotest.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("POST /api/v1/orders - Creating new order");
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{} - Fetching order", orderId);
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) OrderStatus status) {

        log.info("GET /api/v1/orders - Listing orders. Page: {}, PageSize: {}, CustomerId: {}, Status: {}", page, pageSize, customerId, status);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<OrderResponse> response;
        if (customerId != null) {
            response = orderService.getOrdersByCustomerId(customerId, pageable);
        } else if (status != null) {
            response = orderService.getOrdersByStatus(status, pageable);
        } else {
            // For listing all orders, we need a fallback - create a dummy page or modify interface
            response = orderService.getOrdersByStatus(null, pageable);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {

        log.info("PUT /api/v1/orders/{}/status - Updating order status to {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        log.info("DELETE /api/v1/orders/{} - Deleting order", orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        log.info("GET /api/v1/orders/customer/{} - Fetching customer orders", customerId);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<OrderResponse> response = orderService.getOrdersByCustomerId(customerId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/v1/orders/status/{} - Fetching orders by status", status);
        List<OrderResponse> response = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(response);
    }
}


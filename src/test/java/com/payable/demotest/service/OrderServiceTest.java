package com.payable.demotest.service;

import com.payable.demotest.dto.CreateOrderRequest;
import com.payable.demotest.dto.OrderItemRequest;
import com.payable.demotest.dto.OrderResponse;
import com.payable.demotest.entity.Order;
import com.payable.demotest.entity.OrderStatus;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.OrderItemRepository;
import com.payable.demotest.repository.OrderRepository;
import com.payable.demotest.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust_123")
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .items(Arrays.asList(
                        OrderItemRequest.builder()
                                .productId("prod_001")
                                .quantity(2)
                                .unitPrice(BigDecimal.valueOf(29.99))
                                .build()
                ))
                .build();

        Order expectedOrder = Order.builder()
                .orderId("ord_123")
                .customerId("cust_123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        when(orderItemRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("ord_123", response.getOrderId());
        assertEquals("cust_123", response.getCustomerId());
        assertEquals(OrderStatus.PENDING.toString(), response.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_EmptyItems_ThrowsException() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust_123")
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .items(Arrays.asList())
                .build();

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        Order order = Order.builder()
                .orderId("ord_123")
                .customerId("cust_123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(Arrays.asList())
                .build();

        when(orderRepository.findById("ord_123")).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrderById("ord_123");

        // Assert
        assertNotNull(response);
        assertEquals("ord_123", response.getOrderId());
        verify(orderRepository, times(1)).findById("ord_123");
    }

    @Test
    void testGetOrderById_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById("ord_invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById("ord_invalid"));
    }

    @Test
    void testUpdateOrderStatus_Success() {
        // Arrange
        Order order = Order.builder()
                .orderId("ord_123")
                .customerId("cust_123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(Arrays.asList())
                .build();

        when(orderRepository.findById("ord_123")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse response = orderService.updateOrderStatus("ord_123", OrderStatus.CONFIRMED);

        // Assert
        assertNotNull(response);
        verify(orderRepository, times(1)).findById("ord_123");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testDeleteOrder_Success() {
        // Arrange
        Order order = Order.builder()
                .orderId("ord_123")
                .customerId("cust_123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(Arrays.asList())
                .build();

        when(orderRepository.findById("ord_123")).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder("ord_123");

        // Assert
        verify(orderRepository, times(1)).findById("ord_123");
        verify(orderRepository, times(1)).deleteById("ord_123");
    }

    @Test
    void testDeleteOrder_NotPending_ThrowsException() {
        // Arrange
        Order order = Order.builder()
                .orderId("ord_123")
                .customerId("cust_123")
                .status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(Arrays.asList())
                .build();

        when(orderRepository.findById("ord_123")).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> orderService.deleteOrder("ord_123"));
    }
}


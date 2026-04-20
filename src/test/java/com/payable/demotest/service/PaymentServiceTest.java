package com.payable.demotest.service;

import com.payable.demotest.dto.PaymentRequest;
import com.payable.demotest.dto.PaymentResponse;
import com.payable.demotest.entity.Payment;
import com.payable.demotest.entity.PaymentStatus;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.PaymentRepository;
import com.payable.demotest.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository);
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .paymentMethod("CREDIT_CARD")
                .build();

        Payment payment = Payment.builder()
                .paymentId("pay_123")
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .status(PaymentStatus.CAPTURED)
                .paymentMethod("CREDIT_CARD")
                .transactionId("trans_123")
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals("pay_123", response.getPaymentId());
        assertEquals("CAPTURED", response.getStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentById_Success() {
        // Arrange
        Payment payment = Payment.builder()
                .paymentId("pay_123")
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .status(PaymentStatus.CAPTURED)
                .paymentMethod("CREDIT_CARD")
                .transactionId("trans_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById("pay_123")).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPaymentById("pay_123");

        // Assert
        assertNotNull(response);
        assertEquals("pay_123", response.getPaymentId());
        verify(paymentRepository, times(1)).findById("pay_123");
    }

    @Test
    void testGetPaymentByOrderId_Success() {
        // Arrange
        Payment payment = Payment.builder()
                .paymentId("pay_123")
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .status(PaymentStatus.CAPTURED)
                .paymentMethod("CREDIT_CARD")
                .transactionId("trans_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByOrderId("ord_123")).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPaymentByOrderId("ord_123");

        // Assert
        assertNotNull(response);
        assertEquals("ord_123", response.getOrderId());
        verify(paymentRepository, times(1)).findByOrderId("ord_123");
    }

    @Test
    void testRefundPayment_Success() {
        // Arrange
        Payment payment = Payment.builder()
                .paymentId("pay_123")
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .status(PaymentStatus.CAPTURED)
                .paymentMethod("CREDIT_CARD")
                .transactionId("trans_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById("pay_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.refundPayment("pay_123");

        // Assert
        assertNotNull(response);
        assertEquals("pay_123", response.getPaymentId());
        verify(paymentRepository, times(1)).findById("pay_123");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPayment_NotCaptured_ThrowsException() {
        // Arrange
        Payment payment = Payment.builder()
                .paymentId("pay_123")
                .orderId("ord_123")
                .amount(BigDecimal.valueOf(99.99))
                .status(PaymentStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById("pay_123")).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> paymentService.refundPayment("pay_123"));
    }

    @Test
    void testGetPaymentById_NotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById("pay_invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById("pay_invalid"));
    }
}


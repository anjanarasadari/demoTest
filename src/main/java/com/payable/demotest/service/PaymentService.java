package com.payable.demotest.service;

import com.payable.demotest.dto.PaymentRequest;
import com.payable.demotest.dto.PaymentResponse;
import com.payable.demotest.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentByOrderId(String orderId);

    PaymentResponse getPaymentById(String paymentId);

    Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    PaymentResponse refundPayment(String paymentId);
}


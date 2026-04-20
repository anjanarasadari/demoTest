package com.payable.demotest.service.impl;

import com.payable.demotest.dto.PaymentRequest;
import com.payable.demotest.dto.PaymentResponse;
import com.payable.demotest.entity.Payment;
import com.payable.demotest.entity.PaymentStatus;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.PaymentRepository;
import com.payable.demotest.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Simulate payment processing - in real scenario, call payment gateway
        savedPayment.setStatus(PaymentStatus.CAPTURED);
        savedPayment.setProcessedAt(LocalDateTime.now());
        Payment processedPayment = paymentRepository.save(savedPayment);

        log.info("Payment processed successfully. Payment ID: {}, Status: CAPTURED", processedPayment.getPaymentId());
        return mapToResponse(processedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        log.debug("Fetching payment for order: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Payment not found for order: {}", orderId);
                    return new ResourceNotFoundException("Payment", "orderId", orderId);
                });
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId) {
        log.debug("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new ResourceNotFoundException("Payment", "id", paymentId);
                });
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching payments with status: {}", status);
        return paymentRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public PaymentResponse refundPayment(String paymentId) {
        log.info("Initiating refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new ResourceNotFoundException("Payment", "id", paymentId);
                });

        if (!payment.getStatus().equals(PaymentStatus.CAPTURED)) {
            log.error("Cannot refund payment with status: {}", payment.getStatus());
            throw new InvalidOperationException("Can only refund payments in CAPTURED status");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        log.info("Refund processed successfully. Payment ID: {}", paymentId);
        return mapToResponse(refundedPayment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}


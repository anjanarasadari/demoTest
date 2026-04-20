package com.payable.demotest.repository;

import com.payable.demotest.entity.Payment;
import com.payable.demotest.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}


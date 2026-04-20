package com.payable.demotest.repository;

import com.payable.demotest.entity.Shipment;
import com.payable.demotest.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    Optional<Shipment> findByOrderId(String orderId);

    List<Shipment> findByStatus(ShipmentStatus status);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
}


package com.payable.demotest.service;

import com.payable.demotest.dto.ShipmentRequest;
import com.payable.demotest.dto.ShipmentResponse;
import com.payable.demotest.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request);

    ShipmentResponse getShipmentById(String shipmentId);

    ShipmentResponse getShipmentByOrderId(String orderId);

    Page<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable);

    List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status);

    ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus newStatus);

    ShipmentResponse updateTrackingNumber(String shipmentId, String trackingNumber);

    void deleteShipment(String shipmentId);
}


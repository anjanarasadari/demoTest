package com.payable.demotest.controller;

import com.payable.demotest.dto.ShipmentRequest;
import com.payable.demotest.dto.ShipmentResponse;
import com.payable.demotest.entity.ShipmentStatus;
import com.payable.demotest.service.ShipmentService;
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
@RequestMapping("/api/v1/shipments")
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@RequestBody ShipmentRequest request) {
        log.info("POST /api/v1/shipments - Creating new shipment for order: {}", request.getOrderId());
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String shipmentId) {
        log.info("GET /api/v1/shipments/{} - Fetching shipment", shipmentId);
        ShipmentResponse response = shipmentService.getShipmentById(shipmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(@PathVariable String orderId) {
        log.info("GET /api/v1/shipments/order/{} - Fetching shipment for order", orderId);
        ShipmentResponse response = shipmentService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsByStatus(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        log.info("GET /api/v1/shipments - Fetching shipments with status: {}", status);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<ShipmentResponse> response = shipmentService.getShipmentsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByStatusList(@PathVariable ShipmentStatus status) {
        log.info("GET /api/v1/shipments/status/{} - Fetching shipments by status", status);
        List<ShipmentResponse> response = shipmentService.getShipmentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable String shipmentId,
            @RequestParam ShipmentStatus status) {

        log.info("PUT /api/v1/shipments/{}/status - Updating shipment status to {}", shipmentId, status);
        ShipmentResponse response = shipmentService.updateShipmentStatus(shipmentId, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentResponse> updateTrackingNumber(
            @PathVariable String shipmentId,
            @RequestParam String trackingNumber) {

        log.info("PUT /api/v1/shipments/{}/tracking - Updating tracking number to {}", shipmentId, trackingNumber);
        ShipmentResponse response = shipmentService.updateTrackingNumber(shipmentId, trackingNumber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<Void> deleteShipment(@PathVariable String shipmentId) {
        log.info("DELETE /api/v1/shipments/{} - Deleting shipment", shipmentId);
        shipmentService.deleteShipment(shipmentId);
        return ResponseEntity.noContent().build();
    }
}


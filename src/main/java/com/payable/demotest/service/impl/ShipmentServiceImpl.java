package com.payable.demotest.service.impl;

import com.payable.demotest.dto.ShipmentItemResponse;
import com.payable.demotest.dto.ShipmentRequest;
import com.payable.demotest.dto.ShipmentResponse;
import com.payable.demotest.entity.*;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.ShipmentItemRepository;
import com.payable.demotest.repository.ShipmentRepository;
import com.payable.demotest.service.ShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository, ShipmentItemRepository shipmentItemRepository) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentItemRepository = shipmentItemRepository;
    }

    @Override
    public ShipmentResponse createShipment(ShipmentRequest request) {
        log.info("Creating new shipment for order: {}", request.getOrderId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.error("Shipment must contain at least one item");
            throw new InvalidOperationException("Shipment must contain at least one item");
        }

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .status(ShipmentStatus.PENDING)
                .carrierName(request.getCarrierName())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate() != null ?
                    request.getEstimatedDeliveryDate() : LocalDate.now().plusDays(5))
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        log.info("Shipment created successfully with ID: {}", savedShipment.getShipmentId());

        List<ShipmentItem> items = request.getItems().stream()
                .map(itemRequest -> ShipmentItem.builder()
                        .shipment(savedShipment)
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .build())
                .collect(Collectors.toList());

        List<ShipmentItem> savedItems = shipmentItemRepository.saveAll(items);
        savedShipment.setShipmentItems(savedItems);

        return mapToResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(String shipmentId) {
        log.debug("Fetching shipment with ID: {}", shipmentId);
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> {
                    log.error("Shipment not found with ID: {}", shipmentId);
                    return new ResourceNotFoundException("Shipment", "id", shipmentId);
                });
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(String orderId) {
        log.debug("Fetching shipment for order: {}", orderId);
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Shipment not found for order: {}", orderId);
                    return new ResourceNotFoundException("Shipment", "orderId", orderId);
                });
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        log.debug("Fetching shipments with status: {}", status);
        return shipmentRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status) {
        log.debug("Fetching all shipments with status: {}", status);
        return shipmentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus newStatus) {
        log.info("Updating shipment status. Shipment ID: {}, New Status: {}", shipmentId, newStatus);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> {
                    log.error("Shipment not found with ID: {}", shipmentId);
                    return new ResourceNotFoundException("Shipment", "id", shipmentId);
                });

        validateStatusTransition(shipment.getStatus(), newStatus);
        shipment.setStatus(newStatus);

        // Set actual delivery date if status is DELIVERED
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(LocalDate.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        log.info("Shipment status updated successfully. Shipment ID: {}, New Status: {}", shipmentId, newStatus);
        return mapToResponse(updatedShipment);
    }

    @Override
    public ShipmentResponse updateTrackingNumber(String shipmentId, String trackingNumber) {
        log.info("Updating tracking number. Shipment ID: {}, Tracking Number: {}", shipmentId, trackingNumber);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> {
                    log.error("Shipment not found with ID: {}", shipmentId);
                    return new ResourceNotFoundException("Shipment", "id", shipmentId);
                });

        shipment.setTrackingNumber(trackingNumber);
        Shipment updatedShipment = shipmentRepository.save(shipment);

        log.info("Tracking number updated successfully. Shipment ID: {}", shipmentId);
        return mapToResponse(updatedShipment);
    }

    @Override
    public void deleteShipment(String shipmentId) {
        log.info("Deleting shipment with ID: {}", shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> {
                    log.error("Shipment not found with ID: {}", shipmentId);
                    return new ResourceNotFoundException("Shipment", "id", shipmentId);
                });

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            log.error("Cannot delete shipment with status: {}", shipment.getStatus());
            throw new InvalidOperationException("Cannot delete shipment that is not in PENDING status");
        }

        shipmentRepository.deleteById(shipmentId);
        log.info("Shipment deleted successfully with ID: {}", shipmentId);
    }

    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        if (currentStatus == ShipmentStatus.DELIVERED) {
            log.error("Cannot transition from status: {}", currentStatus);
            throw new InvalidOperationException("Cannot transition from " + currentStatus + " status");
        }
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        List<ShipmentItemResponse> itemResponses = shipment.getShipmentItems() != null ?
                shipment.getShipmentItems().stream()
                        .map(item -> ShipmentItemResponse.builder()
                                .shipmentItemId(item.getShipmentItemId())
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return ShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrderId())
                .status(shipment.getStatus().toString())
                .trackingNumber(shipment.getTrackingNumber())
                .carrierName(shipment.getCarrierName())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .shipmentItems(itemResponses)
                .build();
    }
}


package com.payable.demotest.service;

import com.payable.demotest.dto.ShipmentItemRequest;
import com.payable.demotest.dto.ShipmentRequest;
import com.payable.demotest.dto.ShipmentResponse;
import com.payable.demotest.entity.Shipment;
import com.payable.demotest.entity.ShipmentStatus;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.ShipmentItemRepository;
import com.payable.demotest.repository.ShipmentRepository;
import com.payable.demotest.service.impl.ShipmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    private ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentServiceImpl(shipmentRepository, shipmentItemRepository);
    }

    @Test
    void testCreateShipment_Success() {
        // Arrange
        ShipmentRequest request = ShipmentRequest.builder()
                .orderId("ord_123")
                .carrierName("FedEx")
                .items(Arrays.asList(
                        ShipmentItemRequest.builder()
                                .productId("prod_001")
                                .quantity(2)
                                .build()
                ))
                .build();

        Shipment expectedShipment = Shipment.builder()
                .shipmentId("ship_123")
                .orderId("ord_123")
                .status(ShipmentStatus.PENDING)
                .carrierName("FedEx")
                .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(expectedShipment);
        when(shipmentItemRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        ShipmentResponse response = shipmentService.createShipment(request);

        // Assert
        assertNotNull(response);
        assertEquals("ship_123", response.getShipmentId());
        assertEquals("ord_123", response.getOrderId());
        assertEquals(ShipmentStatus.PENDING.toString(), response.getStatus());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(shipmentItemRepository, times(1)).saveAll(any());
    }

    @Test
    void testCreateShipment_EmptyItems_ThrowsException() {
        // Arrange
        ShipmentRequest request = ShipmentRequest.builder()
                .orderId("ord_123")
                .items(Arrays.asList())
                .build();

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> shipmentService.createShipment(request));
    }

    @Test
    void testGetShipmentById_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .orderId("ord_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));

        // Act
        ShipmentResponse response = shipmentService.getShipmentById("ship_123");

        // Assert
        assertNotNull(response);
        assertEquals("ship_123", response.getShipmentId());
        verify(shipmentRepository, times(1)).findById("ship_123");
    }

    @Test
    void testGetShipmentById_NotFound_ThrowsException() {
        // Arrange
        when(shipmentRepository.findById("ship_invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> shipmentService.getShipmentById("ship_invalid"));
    }

    @Test
    void testGetShipmentByOrderId_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .orderId("ord_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findByOrderId("ord_123")).thenReturn(Optional.of(shipment));

        // Act
        ShipmentResponse response = shipmentService.getShipmentByOrderId("ord_123");

        // Assert
        assertNotNull(response);
        assertEquals("ord_123", response.getOrderId());
        verify(shipmentRepository, times(1)).findByOrderId("ord_123");
    }

    @Test
    void testGetShipmentsByStatus_List_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findByStatus(ShipmentStatus.PENDING)).thenReturn(Arrays.asList(shipment));

        // Act
        List<ShipmentResponse> responses = shipmentService.getShipmentsByStatus(ShipmentStatus.PENDING);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(shipmentRepository, times(1)).findByStatus(ShipmentStatus.PENDING);
    }

    @Test
    void testGetShipmentsByStatus_Pageable_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.PENDING)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> page = new PageImpl<>(Arrays.asList(shipment), pageable, 1);

        when(shipmentRepository.findByStatus(ShipmentStatus.PENDING, pageable)).thenReturn(page);

        // Act
        Page<ShipmentResponse> responsePage = shipmentService.getShipmentsByStatus(ShipmentStatus.PENDING, pageable);

        // Assert
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        verify(shipmentRepository, times(1)).findByStatus(ShipmentStatus.PENDING, pageable);
    }

    @Test
    void testUpdateShipmentStatus_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // Act
        ShipmentResponse response = shipmentService.updateShipmentStatus("ship_123", ShipmentStatus.IN_TRANSIT);

        // Assert
        assertNotNull(response);
        assertEquals(ShipmentStatus.IN_TRANSIT.toString(), response.getStatus());
        verify(shipmentRepository, times(1)).findById("ship_123");
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testUpdateShipmentStatus_Delivered_SetsActualDeliveryDate() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShipmentResponse response = shipmentService.updateShipmentStatus("ship_123", ShipmentStatus.DELIVERED);

        // Assert
        assertNotNull(response);
        assertEquals(ShipmentStatus.DELIVERED.toString(), response.getStatus());
        assertEquals(LocalDate.now(), response.getActualDeliveryDate());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testUpdateShipmentStatus_InvalidTransition_ThrowsException() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
                shipmentService.updateShipmentStatus("ship_123", ShipmentStatus.PENDING));
    }

    @Test
    void testUpdateTrackingNumber_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // Act
        ShipmentResponse response = shipmentService.updateTrackingNumber("ship_123", "TRACK123");

        // Assert
        assertNotNull(response);
        assertEquals("TRACK123", response.getTrackingNumber());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testDeleteShipment_Success() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));

        // Act
        shipmentService.deleteShipment("ship_123");

        // Assert
        verify(shipmentRepository, times(1)).deleteById("ship_123");
    }

    @Test
    void testDeleteShipment_NotPending_ThrowsException() {
        // Arrange
        Shipment shipment = Shipment.builder()
                .shipmentId("ship_123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findById("ship_123")).thenReturn(Optional.of(shipment));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> shipmentService.deleteShipment("ship_123"));
    }
}

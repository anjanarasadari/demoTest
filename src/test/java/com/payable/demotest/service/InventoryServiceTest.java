package com.payable.demotest.service;

import com.payable.demotest.dto.InventoryResponse;
import com.payable.demotest.entity.Inventory;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.InventoryRepository;
import com.payable.demotest.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository);
    }

    @Test
    void testGetInventoryByProductId_Success() {
        // Arrange
        Inventory inventory = Inventory.builder()
                .inventoryId("inv_123")
                .productId("prod_001")
                .availableQuantity(100)
                .reservedQuantity(10)
                .allocatedQuantity(5)
                .warehouseLocation("A-1-1")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryRepository.findByProductId("prod_001")).thenReturn(Optional.of(inventory));

        // Act
        InventoryResponse response = inventoryService.getInventoryByProductId("prod_001");

        // Assert
        assertNotNull(response);
        assertEquals("prod_001", response.getProductId());
        assertEquals(100, response.getAvailableQuantity());
        verify(inventoryRepository, times(1)).findByProductId("prod_001");
    }

    @Test
    void testReserveInventory_Success() {
        // Arrange
        Inventory inventory = Inventory.builder()
                .inventoryId("inv_123")
                .productId("prod_001")
                .availableQuantity(100)
                .reservedQuantity(10)
                .allocatedQuantity(5)
                .warehouseLocation("A-1-1")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryRepository.findByProductId("prod_001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        InventoryResponse response = inventoryService.reserveInventory("prod_001", 20);

        // Assert
        assertNotNull(response);
        verify(inventoryRepository, times(1)).findByProductId("prod_001");
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReserveInventory_InsufficientStock_ThrowsException() {
        // Arrange
        Inventory inventory = Inventory.builder()
                .inventoryId("inv_123")
                .productId("prod_001")
                .availableQuantity(5)
                .reservedQuantity(10)
                .allocatedQuantity(5)
                .warehouseLocation("A-1-1")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryRepository.findByProductId("prod_001")).thenReturn(Optional.of(inventory));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> inventoryService.reserveInventory("prod_001", 20));
    }

    @Test
    void testGetInventoryByProductId_NotFound_ThrowsException() {
        // Arrange
        when(inventoryRepository.findByProductId("prod_invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryByProductId("prod_invalid"));
    }

    @Test
    void testAllocateInventory_Success() {
        // Arrange
        Inventory inventory = Inventory.builder()
                .inventoryId("inv_123")
                .productId("prod_001")
                .availableQuantity(100)
                .reservedQuantity(30)
                .allocatedQuantity(5)
                .warehouseLocation("A-1-1")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryRepository.findByProductId("prod_001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        InventoryResponse response = inventoryService.allocateInventory("prod_001", 15);

        // Assert
        assertNotNull(response);
        verify(inventoryRepository, times(1)).findByProductId("prod_001");
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReleaseReservation_Success() {
        // Arrange
        Inventory inventory = Inventory.builder()
                .inventoryId("inv_123")
                .productId("prod_001")
                .availableQuantity(100)
                .reservedQuantity(30)
                .allocatedQuantity(5)
                .warehouseLocation("A-1-1")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryRepository.findByProductId("prod_001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        InventoryResponse response = inventoryService.releaseReservation("prod_001", 10);

        // Assert
        assertNotNull(response);
        verify(inventoryRepository, times(1)).findByProductId("prod_001");
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }
}


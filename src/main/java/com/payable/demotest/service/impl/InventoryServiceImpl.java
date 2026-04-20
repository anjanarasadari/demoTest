package com.payable.demotest.service.impl;

import com.payable.demotest.dto.InventoryResponse;
import com.payable.demotest.entity.Inventory;
import com.payable.demotest.exception.InvalidOperationException;
import com.payable.demotest.exception.ResourceNotFoundException;
import com.payable.demotest.repository.InventoryRepository;
import com.payable.demotest.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(String productId) {
        log.debug("Fetching inventory for product: {}", productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new ResourceNotFoundException("Inventory", "productId", productId);
                });
        return mapToResponse(inventory);
    }

    @Override
    public InventoryResponse reserveInventory(String productId, Integer quantity) {
        log.info("Reserving inventory. Product ID: {}, Quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new ResourceNotFoundException("Inventory", "productId", productId);
                });

        if (inventory.getAvailableQuantity() < quantity) {
            log.error("Insufficient inventory. Available: {}, Requested: {}", inventory.getAvailableQuantity(), quantity);
            throw new InvalidOperationException("Insufficient inventory for product: " + productId);
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Inventory reserved successfully. Product ID: {}, Quantity: {}", productId, quantity);
        return mapToResponse(updatedInventory);
    }

    @Override
    public InventoryResponse releaseReservation(String productId, Integer quantity) {
        log.info("Releasing reserved inventory. Product ID: {}, Quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new ResourceNotFoundException("Inventory", "productId", productId);
                });

        if (inventory.getReservedQuantity() < quantity) {
            log.error("Cannot release more than reserved. Reserved: {}, Release: {}", inventory.getReservedQuantity(), quantity);
            throw new InvalidOperationException("Cannot release more inventory than reserved");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Inventory released successfully. Product ID: {}, Quantity: {}", productId, quantity);
        return mapToResponse(updatedInventory);
    }

    @Override
    public InventoryResponse allocateInventory(String productId, Integer quantity) {
        log.info("Allocating inventory. Product ID: {}, Quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new ResourceNotFoundException("Inventory", "productId", productId);
                });

        if (inventory.getReservedQuantity() < quantity) {
            log.error("Cannot allocate more than reserved. Reserved: {}, Allocate: {}", inventory.getReservedQuantity(), quantity);
            throw new InvalidOperationException("Cannot allocate more inventory than reserved");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAllocatedQuantity(inventory.getAllocatedQuantity() + quantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Inventory allocated successfully. Product ID: {}, Quantity: {}", productId, quantity);
        return mapToResponse(updatedInventory);
    }

    @Override
    public void updateInventory(String productId, Integer newAvailableQuantity) {
        log.info("Updating inventory levels. Product ID: {}, New Available Quantity: {}", productId, newAvailableQuantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new ResourceNotFoundException("Inventory", "productId", productId);
                });

        inventory.setAvailableQuantity(newAvailableQuantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inventory);

        log.info("Inventory updated successfully. Product ID: {}", productId);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .allocatedQuantity(inventory.getAllocatedQuantity())
                .warehouseLocation(inventory.getWarehouseLocation())
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }
}


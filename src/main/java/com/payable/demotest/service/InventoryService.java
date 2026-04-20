package com.payable.demotest.service;

import com.payable.demotest.dto.InventoryResponse;

public interface InventoryService {
    InventoryResponse getInventoryByProductId(String productId);

    InventoryResponse reserveInventory(String productId, Integer quantity);

    InventoryResponse releaseReservation(String productId, Integer quantity);

    InventoryResponse allocateInventory(String productId, Integer quantity);

    void updateInventory(String productId, Integer newAvailableQuantity);
}


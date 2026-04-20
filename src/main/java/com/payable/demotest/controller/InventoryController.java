package com.payable.demotest.controller;

import com.payable.demotest.dto.InventoryResponse;
import com.payable.demotest.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        log.info("GET /api/v1/inventory/{} - Fetching inventory", productId);
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveInventory(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        log.info("POST /api/v1/inventory/{}/reserve - Reserving quantity: {}", productId, quantity);
        InventoryResponse response = inventoryService.reserveInventory(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseInventory(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        log.info("POST /api/v1/inventory/{}/release - Releasing quantity: {}", productId, quantity);
        InventoryResponse response = inventoryService.releaseReservation(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/allocate")
    public ResponseEntity<InventoryResponse> allocateInventory(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        log.info("POST /api/v1/inventory/{}/allocate - Allocating quantity: {}", productId, quantity);
        InventoryResponse response = inventoryService.allocateInventory(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateInventory(
            @PathVariable String productId,
            @RequestParam Integer availableQuantity) {

        log.info("PUT /api/v1/inventory/{} - Updating available quantity to: {}", productId, availableQuantity);
        inventoryService.updateInventory(productId, availableQuantity);
        return ResponseEntity.noContent().build();
    }
}


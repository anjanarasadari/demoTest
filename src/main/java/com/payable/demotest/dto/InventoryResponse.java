package com.payable.demotest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private String inventoryId;
    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer allocatedQuantity;
    private String warehouseLocation;
    private LocalDateTime lastUpdated;
}


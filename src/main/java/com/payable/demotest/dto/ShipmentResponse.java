package com.payable.demotest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {
    private String shipmentId;
    private String orderId;
    private String status;
    private String trackingNumber;
    private String carrierName;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShipmentItemResponse> shipmentItems;
}


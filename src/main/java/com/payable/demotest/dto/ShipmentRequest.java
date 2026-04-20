package com.payable.demotest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {
    private String orderId;
    private String carrierName;
    private LocalDate estimatedDeliveryDate;
    private List<ShipmentItemRequest> items;
}


package com.payable.demotest.config;

import com.payable.demotest.entity.*;
import com.payable.demotest.repository.InventoryRepository;
import com.payable.demotest.repository.OrderRepository;
import com.payable.demotest.repository.PaymentRepository;
import com.payable.demotest.repository.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@Slf4j
public class DataInitializationConfig {

    @Bean
    public CommandLineRunner initializeData(
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ShipmentRepository shipmentRepository) {

        return args -> {
            log.info("Initializing sample data...");

            // Create sample inventory items
            Inventory inv1 = Inventory.builder()
                    .productId("PROD_001")
                    .availableQuantity(100)
                    .reservedQuantity(0)
                    .allocatedQuantity(0)
                    .warehouseLocation("RACK_A1")
                    .lastUpdated(LocalDateTime.now())
                    .build();

            Inventory inv2 = Inventory.builder()
                    .productId("PROD_002")
                    .availableQuantity(50)
                    .reservedQuantity(0)
                    .allocatedQuantity(0)
                    .warehouseLocation("RACK_B2")
                    .lastUpdated(LocalDateTime.now())
                    .build();

            Inventory inv3 = Inventory.builder()
                    .productId("PROD_003")
                    .availableQuantity(200)
                    .reservedQuantity(0)
                    .allocatedQuantity(0)
                    .warehouseLocation("RACK_C3")
                    .lastUpdated(LocalDateTime.now())
                    .build();

            inventoryRepository.save(inv1);
            inventoryRepository.save(inv2);
            inventoryRepository.save(inv3);

            log.info("Sample inventory items initialized successfully");

            // Create sample order
            Order order = Order.builder()
                    .customerId("CUST_001")
                    .status(OrderStatus.PENDING)
                    .totalAmount(new BigDecimal("199.98"))
                    .shippingAddress("123 Main St, New York, NY 10001")
                    .billingAddress("123 Main St, New York, NY 10001")
                    .build();

            Order savedOrder = orderRepository.save(order);
            log.info("Sample order created with ID: {}", savedOrder.getOrderId());

            // Create sample payment
            Payment payment = Payment.builder()
                    .orderId(savedOrder.getOrderId())
                    .amount(new BigDecimal("199.98"))
                    .status(PaymentStatus.PENDING)
                    .paymentMethod("CREDIT_CARD")
                    .build();

            paymentRepository.save(payment);
            log.info("Sample payment created");

            // Create sample shipment
            Shipment shipment = Shipment.builder()
                    .orderId(savedOrder.getOrderId())
                    .status(ShipmentStatus.PENDING)
                    .carrierName("FedEx")
                    .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                    .build();

            shipmentRepository.save(shipment);
            log.info("Sample shipment created");

            log.info("Data initialization completed successfully");
        };
    }
}


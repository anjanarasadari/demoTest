# Order Management System - Testing Guide

## Quick Start Testing

The application comes with sample data pre-loaded. Start the application and test immediately!

```bash
./mvnw spring-boot:run
```

Then test endpoints at: `http://localhost:8080/api/v1`

---

## Test Scenarios

### Scenario 1: Complete Order Flow

#### Step 1: Check Available Inventory

```bash
GET http://localhost:8080/api/v1/inventory/PROD_001

Response:
{
  "inventoryId": "...",
  "productId": "PROD_001",
  "availableQuantity": 100,
  "reservedQuantity": 0,
  "allocatedQuantity": 0,
  "warehouseLocation": "RACK_A1",
  "lastUpdated": "2024-04-18T10:30:00"
}
```

#### Step 2: Create an Order

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "CUST_NEW",
  "shippingAddress": "456 Oak Ave, Los Angeles, CA 90001",
  "billingAddress": "456 Oak Ave, Los Angeles, CA 90001",
  "items": [
    {
      "productId": "PROD_001",
      "quantity": 5,
      "unitPrice": 99.99
    },
    {
      "productId": "PROD_002",
      "quantity": 3,
      "unitPrice": 49.99
    }
  ]
}

Response: 201 Created
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST_NEW",
  "status": "PENDING",
  "totalAmount": 649.92,
  "shippingAddress": "456 Oak Ave, Los Angeles, CA 90001",
  "billingAddress": "456 Oak Ave, Los Angeles, CA 90001",
  "createdAt": "2024-04-18T11:00:00",
  "updatedAt": "2024-04-18T11:00:00",
  "orderItems": [
    {
      "orderItemId": "...",
      "productId": "PROD_001",
      "quantity": 5,
      "unitPrice": 99.99,
      "totalPrice": 499.95,
      "status": "RESERVED"
    },
    {
      "orderItemId": "...",
      "productId": "PROD_002",
      "quantity": 3,
      "unitPrice": 49.99,
      "totalPrice": 149.97,
      "status": "RESERVED"
    }
  ]
}
```

**Note:** Save the `orderId` for next steps!

#### Step 3: Retrieve Order Details

```bash
GET http://localhost:8080/api/v1/orders/550e8400-e29b-41d4-a716-446655440000

Response: 200 OK (same as order creation response)
```

#### Step 4: Process Payment

```bash
POST http://localhost:8080/api/v1/payments
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 649.92,
  "paymentMethod": "CREDIT_CARD"
}

Response: 201 Created
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440001",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 649.92,
  "status": "CAPTURED",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "550e8400-e29b-41d4-a716-446655440002",
  "processedAt": "2024-04-18T11:00:05",
  "createdAt": "2024-04-18T11:00:00",
  "updatedAt": "2024-04-18T11:00:05"
}
```

#### Step 5: Update Order Status to CONFIRMED

```bash
PUT http://localhost:8080/api/v1/orders/550e8400-e29b-41d4-a716-446655440000/status?status=CONFIRMED

Response: 200 OK
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST_NEW",
  "status": "CONFIRMED",
  "totalAmount": 649.92,
  ...
}
```

#### Step 6: Create Shipment

```bash
POST http://localhost:8080/api/v1/shipments
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "carrierName": "UPS",
  "estimatedDeliveryDate": "2024-04-25",
  "items": [
    {
      "productId": "PROD_001",
      "quantity": 5
    },
    {
      "productId": "PROD_002",
      "quantity": 3
    }
  ]
}

Response: 201 Created
{
  "shipmentId": "550e8400-e29b-41d4-a716-446655440003",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "trackingNumber": null,
  "carrierName": "UPS",
  "estimatedDeliveryDate": "2024-04-25",
  "actualDeliveryDate": null,
  "createdAt": "2024-04-18T11:00:10",
  "updatedAt": "2024-04-18T11:00:10",
  "shipmentItems": [...]
}
```

**Note:** Save the `shipmentId` for next steps!

#### Step 7: Update Shipment with Tracking Number

```bash
PUT http://localhost:8080/api/v1/shipments/550e8400-e29b-41d4-a716-446655440003/tracking?trackingNumber=1Z999AA10123456784

Response: 200 OK
{
  "shipmentId": "550e8400-e29b-41d4-a716-446655440003",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "trackingNumber": "1Z999AA10123456784",
  ...
}
```

#### Step 8: Update Shipment Status Through Workflow

**Status: PICKED**
```bash
PUT http://localhost:8080/api/v1/shipments/550e8400-e29b-41d4-a716-446655440003/status?status=PICKED
```

**Status: PACKED**
```bash
PUT http://localhost:8080/api/v1/shipments/550e8400-e29b-41d4-a716-446655440003/status?status=PACKED
```

**Status: IN_TRANSIT**
```bash
PUT http://localhost:8080/api/v1/shipments/550e8400-e29b-41d4-a716-446655440003/status?status=IN_TRANSIT
```

**Status: DELIVERED**
```bash
PUT http://localhost:8080/api/v1/shipments/550e8400-e29b-41d4-a716-446655440003/status?status=DELIVERED

Response: 200 OK
{
  ...
  "status": "DELIVERED",
  "actualDeliveryDate": "2024-04-18"
}
```

---

### Scenario 2: Inventory Management

#### Check Inventory Levels

```bash
GET http://localhost:8080/api/v1/inventory/PROD_001
GET http://localhost:8080/api/v1/inventory/PROD_002
GET http://localhost:8080/api/v1/inventory/PROD_003
```

#### Reserve Inventory

```bash
POST http://localhost:8080/api/v1/inventory/PROD_001/reserve?quantity=10

Response: 200 OK
{
  "productId": "PROD_001",
  "availableQuantity": 90,
  "reservedQuantity": 10,
  "allocatedQuantity": 0
}
```

#### Allocate Reserved Inventory

```bash
POST http://localhost:8080/api/v1/inventory/PROD_001/allocate?quantity=10

Response: 200 OK
{
  "productId": "PROD_001",
  "availableQuantity": 90,
  "reservedQuantity": 0,
  "allocatedQuantity": 10
}
```

#### Release Reservation (Rollback)

```bash
POST http://localhost:8080/api/v1/inventory/PROD_001/release?quantity=5

Response: 200 OK
{
  "productId": "PROD_001",
  "availableQuantity": 95,
  "reservedQuantity": 0,
  "allocatedQuantity": 10
}
```

#### Update Inventory Stock

```bash
PUT http://localhost:8080/api/v1/inventory/PROD_001?availableQuantity=150

Response: 204 No Content
```

---

### Scenario 3: Payment Refunds

#### Get Payment Details

```bash
GET http://localhost:8080/api/v1/payments/550e8400-e29b-41d4-a716-446655440001

Response: 200 OK
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440001",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 649.92,
  "status": "CAPTURED",
  ...
}
```

#### Refund Payment

```bash
POST http://localhost:8080/api/v1/payments/550e8400-e29b-41d4-a716-446655440001/refund

Response: 200 OK
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440001",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 649.92,
  "status": "REFUNDED",
  ...
}
```

---

### Scenario 4: Pagination & Filtering

#### List Orders with Pagination

```bash
GET http://localhost:8080/api/v1/orders?page=0&pageSize=10

Response: 200 OK
{
  "content": [...],
  "pageable": {
    "sort": {...},
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 15,
  "totalPages": 2,
  "last": false,
  "size": 10,
  "number": 0,
  "sort": {...},
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

#### Get Orders by Customer

```bash
GET http://localhost:8080/api/v1/orders/customer/CUST_001?page=0&pageSize=5

Response: 200 OK (paginated results for specific customer)
```

#### Get Orders by Status

```bash
GET http://localhost:8080/api/v1/orders/status/PENDING

Response: 200 OK
[
  { "orderId": "...", "status": "PENDING", ... },
  { "orderId": "...", "status": "PENDING", ... }
]
```

#### Get Payments by Status

```bash
GET http://localhost:8080/api/v1/payments?status=CAPTURED&page=0&pageSize=20

Response: 200 OK (paginated payment results)
```

---

### Scenario 5: Error Handling

#### Order Not Found

```bash
GET http://localhost:8080/api/v1/orders/invalid-uuid

Response: 400 Bad Request
{
  "message": "Order not found with ID: invalid-uuid",
  "error": "Bad Request",
  "status": 400,
  "timestamp": "2024-04-18T11:00:00",
  "path": "/api/v1/orders/invalid-uuid"
}
```

#### Insufficient Inventory

```bash
POST http://localhost:8080/api/v1/inventory/PROD_001/reserve?quantity=1000

Response: 400 Bad Request
{
  "message": "Insufficient inventory for product: PROD_001",
  "error": "Bad Request",
  "status": 400,
  "timestamp": "2024-04-18T11:00:00",
  "path": "/api/v1/inventory/PROD_001/reserve"
}
```

#### Cannot Refund Non-Captured Payment

```bash
POST http://localhost:8080/api/v1/payments/invalid-payment-id/refund

Response: 400 Bad Request
{
  "message": "Can only refund payments in CAPTURED status",
  "error": "Bad Request",
  "status": 400,
  ...
}
```

#### Cannot Delete Non-Pending Order

```bash
DELETE http://localhost:8080/api/v1/orders/550e8400-e29b-41d4-a716-446655440000

Response: 400 Bad Request
{
  "message": "Cannot delete order that is not in PENDING or CANCELLED status",
  "error": "Bad Request",
  "status": 400,
  ...
}
```

---

## Test Data Reference

### Pre-loaded Inventory

| Product ID | Available | Reserved | Allocated | Location |
|-----------|-----------|----------|-----------|----------|
| PROD_001  | 100       | 0        | 0         | RACK_A1  |
| PROD_002  | 50        | 0        | 0         | RACK_B2  |
| PROD_003  | 200       | 0        | 0         | RACK_C3  |

### Pre-loaded Orders

- **Order ID**: (UUID) - Status: PENDING
- **Customer ID**: CUST_001
- **Total Amount**: $199.98

---

## Performance Testing

### Test Load

Create multiple orders concurrently:

```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "CUST_'$i'",
      "shippingAddress": "Address '$i'",
      "billingAddress": "Address '$i'",
      "items": [{"productId": "PROD_001", "quantity": 1, "unitPrice": 99.99}]
    }' &
done
wait
```

### Response Time Targets

- Order Creation: < 500ms
- Order Retrieval: < 100ms
- Inventory Check: < 50ms
- Payment Processing: < 1s

---

## Test Validation Checklist

- [ ] All CRUD operations work correctly
- [ ] Pagination returns correct number of records
- [ ] Filtering by status works
- [ ] Inventory quantities update correctly
- [ ] Payment status transitions work
- [ ] Shipment workflow progresses correctly
- [ ] Error responses return appropriate HTTP status codes
- [ ] Error messages are meaningful
- [ ] Transactions are atomic (all or nothing)
- [ ] Timestamps are set correctly
- [ ] Logging captures all operations

---

## Debugging Tips

### Enable SQL Logging

Add to `application.properties`:
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Check Database State

Access H2 Console:
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:order_management
```

### View Application Logs

Logs are output to console with:
- ERROR: Red
- WARN: Yellow
- INFO: Default
- DEBUG: Detailed operations

---

## Integration Test Automation (Future)

You can add JUnit 5 + Mockito tests:

```java
@SpringBootTest
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateOrder() throws Exception {
        // Test implementation
    }
}
```

---

End of Testing Guide


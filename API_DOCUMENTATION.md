# Order Management System - API Documentation

## Overview
This is a scalable order management system with complete CRUD operations for Orders, Payments, Inventory, and Shipments.

## Base URL
```
http://localhost:8080/api/v1
```

## Database Access
- H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:order_management`
- Username: `sa`
- Password: (leave empty)

---

## Order Management

### Create Order
**POST** `/orders`

```json
{
  "customerId": "CUST_001",
  "shippingAddress": "123 Main St, New York, NY 10001",
  "billingAddress": "123 Main St, New York, NY 10001",
  "items": [
    {
      "productId": "PROD_001",
      "quantity": 2,
      "unitPrice": 99.99
    },
    {
      "productId": "PROD_002",
      "quantity": 1,
      "unitPrice": 49.99
    }
  ]
}
```

**Response: 201 Created**
```json
{
  "orderId": "uuid-string",
  "customerId": "CUST_001",
  "status": "PENDING",
  "totalAmount": 249.97,
  "shippingAddress": "123 Main St, New York, NY 10001",
  "billingAddress": "123 Main St, New York, NY 10001",
  "createdAt": "2024-04-18T10:30:00",
  "updatedAt": "2024-04-18T10:30:00",
  "orderItems": [
    {
      "orderItemId": "uuid-string",
      "productId": "PROD_001",
      "quantity": 2,
      "unitPrice": 99.99,
      "totalPrice": 199.98,
      "status": "RESERVED"
    }
  ]
}
```

### Get Order By ID
**GET** `/orders/{orderId}`

**Response: 200 OK** - Returns OrderResponse (see above)

### List All Orders
**GET** `/orders`

**Query Parameters:**
- `page`: Page number (default: 0)
- `pageSize`: Records per page (default: 20)
- `customerId`: Filter by customer (optional)
- `status`: Filter by order status (optional) - PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

**Response: 200 OK**
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Get Customer Orders
**GET** `/orders/customer/{customerId}`

**Query Parameters:**
- `page`: Page number (default: 0)
- `pageSize`: Records per page (default: 20)

### Get Orders By Status
**GET** `/orders/status/{status}`

Returns list of all orders with the specified status.

### Update Order Status
**PUT** `/orders/{orderId}/status`

**Query Parameters:**
- `status`: New status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

**Response: 200 OK** - Returns updated OrderResponse

### Delete Order
**DELETE** `/orders/{orderId}`

**Response: 204 No Content**

---

## Payment Management

### Process Payment
**POST** `/payments`

```json
{
  "orderId": "uuid-string",
  "amount": 249.97,
  "paymentMethod": "CREDIT_CARD"
}
```

**Response: 201 Created**
```json
{
  "paymentId": "uuid-string",
  "orderId": "uuid-string",
  "amount": 249.97,
  "status": "CAPTURED",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "uuid-string",
  "processedAt": "2024-04-18T10:30:05",
  "createdAt": "2024-04-18T10:30:00",
  "updatedAt": "2024-04-18T10:30:05"
}
```

### Get Payment By ID
**GET** `/payments/{paymentId}`

**Response: 200 OK** - Returns PaymentResponse (see above)

### Get Payment By Order ID
**GET** `/payments/order/{orderId}`

**Response: 200 OK** - Returns PaymentResponse

### Get Payments By Status
**GET** `/payments`

**Query Parameters:**
- `status`: Payment status (PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED)
- `page`: Page number (default: 0)
- `pageSize`: Records per page (default: 20)

**Response: 200 OK** - Returns paginated PaymentResponse list

### Refund Payment
**POST** `/payments/{paymentId}/refund`

**Response: 200 OK** - Returns updated PaymentResponse with status REFUNDED

---

## Inventory Management

### Get Inventory By Product ID
**GET** `/inventory/{productId}`

**Response: 200 OK**
```json
{
  "inventoryId": "uuid-string",
  "productId": "PROD_001",
  "availableQuantity": 100,
  "reservedQuantity": 0,
  "allocatedQuantity": 0,
  "warehouseLocation": "RACK_A1",
  "lastUpdated": "2024-04-18T10:30:00"
}
```

### Reserve Inventory
**POST** `/inventory/{productId}/reserve`

**Query Parameters:**
- `quantity`: Quantity to reserve (required)

**Response: 200 OK** - Returns updated InventoryResponse

### Release Inventory Reservation
**POST** `/inventory/{productId}/release`

**Query Parameters:**
- `quantity`: Quantity to release (required)

**Response: 200 OK** - Returns updated InventoryResponse

### Allocate Inventory
**POST** `/inventory/{productId}/allocate`

**Query Parameters:**
- `quantity`: Quantity to allocate (required)

**Response: 200 OK** - Returns updated InventoryResponse

### Update Inventory Levels
**PUT** `/inventory/{productId}`

**Query Parameters:**
- `availableQuantity`: New available quantity (required)

**Response: 204 No Content**

---

## Shipment Management

### Create Shipment
**POST** `/shipments`

```json
{
  "orderId": "uuid-string",
  "carrierName": "FedEx",
  "estimatedDeliveryDate": "2024-04-23",
  "items": [
    {
      "productId": "PROD_001",
      "quantity": 2
    }
  ]
}
```

**Response: 201 Created**
```json
{
  "shipmentId": "uuid-string",
  "orderId": "uuid-string",
  "status": "PENDING",
  "trackingNumber": null,
  "carrierName": "FedEx",
  "estimatedDeliveryDate": "2024-04-23",
  "actualDeliveryDate": null,
  "createdAt": "2024-04-18T10:30:00",
  "updatedAt": "2024-04-18T10:30:00",
  "shipmentItems": [
    {
      "shipmentItemId": "uuid-string",
      "productId": "PROD_001",
      "quantity": 2
    }
  ]
}
```

### Get Shipment By ID
**GET** `/shipments/{shipmentId}`

**Response: 200 OK** - Returns ShipmentResponse (see above)

### Get Shipment By Order ID
**GET** `/shipments/order/{orderId}`

**Response: 200 OK** - Returns ShipmentResponse

### Get Shipments By Status
**GET** `/shipments`

**Query Parameters:**
- `status`: Shipment status (PENDING, PICKED, PACKED, IN_TRANSIT, DELIVERED, FAILED)
- `page`: Page number (default: 0)
- `pageSize`: Records per page (default: 20)

**Response: 200 OK** - Returns paginated ShipmentResponse list

### Get All Shipments With Status (List)
**GET** `/shipments/status/{status}`

**Response: 200 OK** - Returns list of ShipmentResponse

### Update Shipment Status
**PUT** `/shipments/{shipmentId}/status`

**Query Parameters:**
- `status`: New status (PENDING, PICKED, PACKED, IN_TRANSIT, DELIVERED, FAILED)

**Response: 200 OK** - Returns updated ShipmentResponse

### Update Tracking Number
**PUT** `/shipments/{shipmentId}/tracking`

**Query Parameters:**
- `trackingNumber`: Tracking number (required)

**Response: 200 OK** - Returns updated ShipmentResponse

### Delete Shipment
**DELETE** `/shipments/{shipmentId}`

**Response: 204 No Content**

---

## Error Handling

### Error Response Format
```json
{
  "message": "Detailed error message",
  "error": "Error Type",
  "status": 400,
  "timestamp": "2024-04-18T10:30:00",
  "path": "/api/v1/orders/invalid-id"
}
```

### Error Status Codes
- **400 Bad Request** - Invalid input or business logic violation
- **404 Not Found** - Resource not found
- **409 Conflict** - Operation cannot be completed due to state conflict
- **500 Internal Server Error** - Unexpected server error

---

## Enum Values

### OrderStatus
```
PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
```

### OrderItemStatus
```
RESERVED, CONFIRMED, PICKED, SHIPPED, DELIVERED, CANCELLED
```

### PaymentStatus
```
PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED
```

### ShipmentStatus
```
PENDING, PICKED, PACKED, IN_TRANSIT, DELIVERED, FAILED
```

### ShipmentItemStatus
```
PICKED, PACKED, IN_TRANSIT, DELIVERED, LOST
```

---

## Sample cURL Commands

### Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST_001",
    "shippingAddress": "123 Main St, New York, NY 10001",
    "billingAddress": "123 Main St, New York, NY 10001",
    "items": [
      {
        "productId": "PROD_001",
        "quantity": 2,
        "unitPrice": 99.99
      }
    ]
  }'
```

### Get Inventory
```bash
curl http://localhost:8080/api/v1/inventory/PROD_001
```

### Process Payment
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "uuid-string",
    "amount": 199.98,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Create Shipment
```bash
curl -X POST http://localhost:8080/api/v1/shipments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "uuid-string",
    "carrierName": "FedEx",
    "estimatedDeliveryDate": "2024-04-23",
    "items": [
      {
        "productId": "PROD_001",
        "quantity": 2
      }
    ]
  }'
```

---

## Architecture Overview

### Layered Architecture
1. **Controller Layer** - HTTP request handling and routing
2. **Service Layer** - Business logic and domain operations
3. **Repository Layer** - Data access abstraction
4. **Entity Layer** - Domain models and database mappings
5. **Exception Handling** - Centralized error management

### Key Features
- **Transaction Management** - ACID compliance for critical operations
- **Pagination Support** - Efficient data retrieval for large result sets
- **Comprehensive Logging** - DEBUG level for troubleshooting, INFO for business events
- **Error Handling** - Custom exceptions and global exception handler
- **Data Initialization** - Sample data loaded on startup for testing

### Database
- **Development**: H2 In-Memory Database
- **Production**: MySQL
- **Schema**: Auto-generated by Hibernate with `create-drop` for H2 and `update` for MySQL


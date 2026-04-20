# Scalable Order Management System Design

## Executive Summary

Build a microservices-based order management system with event-driven architecture, supporting multiple deployment patterns. The system will balance consistency (ACID transactions, saga orchestration) against availability (eventual consistency, event sourcing), and throughput (async processing, event streaming) against complexity (operational overhead, monitoring requirements). Trade-offs exist between strong consistency and horizontal scalability, read performance and write consistency, and feature richness versus operational simplicity.

## Architecture Overview

### Option 1: Monolithic with Clear Service Boundaries (Starting Point)
- **Pros:** Simple deployment, strong consistency, easier debugging
- **Cons:** Coarse-grained scaling, tight coupling internally
- **Trade-off:** Prioritizes simplicity over horizontal scalability

### Option 2: Microservices with Event-Driven Architecture (Recommended for Scale)
- **Pros:** Independent scaling, loose coupling, technology flexibility
- **Cons:** Distributed transaction complexity, operational overhead, network latency
- **Trade-off:** Prioritizes scalability over operational simplicity

### Option 3: Hybrid Monolithic + Async Event Processing
- **Pros:** Single deployment unit, async processing for long-running tasks
- **Cons:** Still limited horizontal scaling for peak loads
- **Trade-off:** Balance between simplicity and throughput improvements

## Core Domain Models

### 1. Order Aggregate
```
Order
├── OrderId (unique identifier)
├── CustomerId
├── Status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
├── CreatedAt
├── UpdatedAt
├── OrderItems[] (list of items in order)
├── TotalAmount
├── ShippingAddress
└── BillingAddress
```

**Invariants:**
- Order total must equal sum of all item prices
- Order cannot transition from DELIVERED back to PENDING
- Order must have at least one item
- Payment must be successful before transitioning to PROCESSING

### 2. OrderItem Entity
```
OrderItem
├── OrderItemId
├── OrderId (foreign key)
├── ProductId
├── Quantity
├── UnitPrice
├── TotalPrice
└── Status (RESERVED, CONFIRMED, PICKED, SHIPPED, DELIVERED, CANCELLED)
```

### 3. Payment Aggregate
```
Payment
├── PaymentId
├── OrderId
├── Amount
├── Status (PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED)
├── PaymentMethod
├── ProcessedAt
├── TransactionId (from payment gateway)
└── Metadata
```

**Invariants:**
- Payment amount must match order total
- Only one payment per order (or implement retry logic for failed payments)
- Payment status must be CAPTURED before order can proceed to fulfillment

### 4. Inventory Aggregate
```
Inventory
├── InventoryId
├── ProductId
├── AvailableQuantity
├── ReservedQuantity
├── AllocatedQuantity
├── WarehouseLocation
└── LastUpdated
```

**Invariants:**
- AvailableQuantity + ReservedQuantity + AllocatedQuantity must equal total stock
- No negative quantities allowed
- Reservations must expire if not confirmed within timeout window

### 5. Shipment Aggregate
```
Shipment
├── ShipmentId
├── OrderId
├── Status (PENDING, PICKED, PACKED, IN_TRANSIT, DELIVERED, FAILED)
├── TrackingNumber
├── CarrierName
├── EstimatedDeliveryDate
├── ActualDeliveryDate
└── Items[] (items in this shipment)
```


### Partitioning Strategy

**Option 1: Partition by CustomerId**
- Benefit: All orders for a customer co-located, efficient filtering
- Drawback: Uneven distribution if some customers are much more active
- Recommended for: B2B systems or customer-focused analytics

**Option 2: Partition by OrderId (Hash-based)**
- Benefit: Even distribution across partitions
- Drawback: Cross-customer queries require scatter-gather
- Recommended for: High-volume e-commerce systems

**Option 3: Range-based by CreatedDate**
- Benefit: Time-series queries are efficient, archival is simple
- Drawback: Hot-cold partition problem (recent partitions get more traffic)
- Recommended for: Systems with strong time-based analysis needs

### Caching Strategy
**Redis Caching Layer:**
```
- Order Summary Cache: TTL 5 minutes
- Inventory Levels Cache: TTL 1 minute (high volatility)
- Customer Preferences Cache: TTL 24 hours
- Product Catalog Cache: TTL 12 hours

Cache Invalidation: Event-driven invalidation on updates
```

**Trade-off:** Reduced latency vs. potential stale data consistency

## Event-Driven Async Processing

### Event Sourcing Pattern
**Core Idea:** Store the sequence of events that led to current state, rather than just the current state.

**Events to capture:**
1. `OrderCreated` — Order placed in system
2. `OrderConfirmed` — Order confirmed by customer
3. `PaymentProcessed` — Payment successful
4. `PaymentFailed` — Payment declined
5. `InventoryReserved` — Items reserved from inventory
6. `InventoryReservationFailed` — Insufficient stock
7. `InventoryAllocated` — Items moved to picking queue
8. `ShipmentCreated` — Shipment created
9. `ShipmentDispatched` — Shipment left warehouse
10. `OrderDelivered` — Order delivered to customer
11. `OrderCancelled` — Order cancelled
12. `PaymentRefunded` — Refund processed

**Advantages:**
- Complete audit trail for compliance and debugging
- Event replay for recovery and testing
- Easy temporal queries ("show me state at time T")
- Natural fit for event-driven microservices

**Disadvantages:**
- Increased storage requirements (all events retained)
- Event store complexity (versioning, upcasting)
- Eventual consistency between event store and read models
- Learning curve for teams

### Message Broker: Kafka vs. RabbitMQ

**Kafka (Event Streaming):**
- Pros: High throughput, event replay, consumer groups for scalability, durability
- Cons: Higher operational complexity, requires cluster, not ideal for request-reply
- Recommended for: High-volume order processing, event sourcing, analytics

**RabbitMQ (Message Queue):**
- Pros: Simple deployment, routing flexibility, AMQP protocol
- Cons: Lower throughput than Kafka, limited replay capability
- Recommended for: Moderate volumes, traditional request-reply patterns

**Recommendation:** Kafka for order events (high volume, replay needed), RabbitMQ for synchronous request-reply (payment gateway, inventory service)

### Saga Pattern for Distributed Transactions

**Choreography Pattern (Event-Driven):**
```
1. Order Service emits OrderCreated event
2. Payment Service listens, processes payment, emits PaymentProcessed
3. Inventory Service listens, reserves stock, emits InventoryReserved
4. Fulfillment Service listens, creates shipment
5. If any step fails, compensating transactions triggered
```

**Pros:** Decoupled services, no central coordinator
**Cons:** Difficult to debug, implicit workflow hard to visualize

**Orchestration Pattern (Centralized):**
```
1. Saga Orchestrator listens for OrderCreated
2. Orchestrator explicitly calls Payment Service → Inventory Service → Fulfillment Service
3. On failure, orchestrator calls compensation services
4. Orchestrator maintains state machine of saga execution
```

**Pros:** Clear workflow, easier debugging, centralized control
**Cons:** Orchestrator becomes bottleneck/single point of failure

**Recommendation:** Use Choreography for simple happy-path (2-3 steps); use Orchestration for complex workflows with many conditional branches

## Resilience Patterns

### Circuit Breaker Pattern
```
States: CLOSED → OPEN → HALF_OPEN → CLOSED

CLOSED (Normal): All calls go through
OPEN (Failing): After N failures in M seconds, reject calls immediately
HALF_OPEN (Testing): After timeout, allow trial request; if succeeds → CLOSED, if fails → OPEN

Implementation: Netflix Hystrix, Spring Cloud Circuit Breaker, Resilience4j
```

### Retry Strategy
```
Exponential backoff with jitter:
Retry 1: Wait 1s + random(0-100ms)
Retry 2: Wait 2s + random(0-100ms)
Retry 3: Wait 4s + random(0-100ms)
Max retries: 3 (configurable per service)

Apply only to idempotent operations (GET, DELETE) or operations with idempotency keys
```

### Timeout Strategy
```
Service Call Timeout: 5 seconds (fail fast)
Saga Compensation Timeout: 30 seconds (allow more time for cleanup)
Database Query Timeout: 10 seconds (prevent hung queries)
```

### Idempotency Keys
```
Every order creation request includes:
X-Idempotency-Key: uuid
- Server stores (idempotency_key, order_id) mapping
- Duplicate requests with same key return cached response
- Prevents duplicate orders from retries

Expire idempotency keys after 24 hours
```

### Bulkhead Pattern
```
Separate thread pools for different operations:
- Thread Pool 1 (10 threads): Payment processing
- Thread Pool 2 (20 threads): Inventory queries
- Thread Pool 3 (5 threads): Shipment notifications

Prevents one slow service from exhausting all resources
```

## API Design & Versioning

### REST Endpoints (Primary)
```
POST   /api/v1/orders                        Create new order
GET    /api/v1/orders/{orderId}              Get order details
GET    /api/v1/orders                        List orders (with pagination, filtering)
PUT    /api/v1/orders/{orderId}              Update order (limited fields)
DELETE /api/v1/orders/{orderId}              Cancel order
GET    /api/v1/orders/{orderId}/status       Get order status (lightweight)

POST   /api/v1/orders/{orderId}/payment      Process payment
GET    /api/v1/orders/{orderId}/shipment     Get shipment details
POST   /api/v1/orders/{orderId}/cancel       Cancel order (with reason)

GET    /api/v1/inventory/{productId}        Check inventory levels
```

### Request/Response Structure
```json
// POST /api/v1/orders
{
  "customerId": "cust_123",
  "items": [
    {
      "productId": "prod_456",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "shippingAddress": { ... },
  "billingAddress": { ... }
}

// Response (202 Accepted for async processing)
{
  "orderId": "ord_789",
  "status": "PENDING",
  "createdAt": "2026-04-18T10:30:00Z",
  "estimatedDeliveryDate": "2026-04-25",
  "_links": {
    "self": { "href": "/api/v1/orders/ord_789" },
    "status": { "href": "/api/v1/orders/ord_789/status" },
    "payment": { "href": "/api/v1/orders/ord_789/payment" }
  }
}
```

### Pagination
```
GET /api/v1/orders?page=1&pageSize=20&sort=createdAt:desc&status=PENDING

Response includes:
- data[]
- pagination: { page, pageSize}
- _links: { first, last, next, prev }
```

### Versioning Strategy
**URL-based versioning:** `/api/v1/` vs `/api/v2/`
- Pros: Explicit, easy to maintain multiple versions
- Cons: Code duplication, multiple deployment versions
- **Recommended for:** APIs with breaking changes

**Header-based versioning:** `Accept: application/vnd.myapi.v2+json`
- Pros: Single deployment, cleaner URLs
- Cons: Harder to enforce, less explicit
- **Recommended for:** Minor versions, gradual deprecation

**Backwards-compatible evolution:**
- Add new optional fields (existing clients ignore them)
- Use field aliases for renamed fields
- Never change field types (breaking)
- Deprecate fields with warnings (3-6 month notice period)

## Scalability Dimensions

### Horizontal Scaling
**Stateless Services:** Order API, Payment API, Fulfillment API
- Load balance with Round Robin or Least Connections
- Add replicas dynamically based on CPU/Memory metrics

**Stateful Components:** 
- Event Store (append-only): Partitioned by topic/aggregate ID
- Cache (Redis): Cluster mode or sentinel-based HA
- Message Queue (Kafka): Partition-based consumer groups

### Vertical Scaling
- Increase JVM heap for Spring Boot services
- Optimize database indexes on high-volume queries
- Implement connection pooling (HikariCP)

### Read/Write Separation (CQRS)
```
Write Model: Normalized schema optimized for consistency
- Orders table: Join-heavy for integrity checks
- Transactions: ACID properties enforced

Read Model: Denormalized for query speed
- Orders_Denormalized: Pre-computed aggregates, materialized views
- Analytics_Orders: Aggregated data for dashboards
- Updated async via event handlers
```

## Monitoring & Observability

### Metrics to Track
```
Application Metrics:
- Orders created per minute
- Order completion time (p50, p95, p99)
- Payment success rate
- Inventory reservation success rate
- Saga compensation triggers

Infrastructure Metrics:
- Request latency by endpoint
- Error rates by service
- Database query performance
- Cache hit rate
- Message queue lag (Kafka consumer lag)

Business Metrics:
- Revenue per order
- Average order value
- Customer churn rate
- Peak transaction volumes
```

### Distributed Tracing
**Tools:** Jaeger, Zipkin
```
Trace Order → Payment → Inventory → Shipment in single request
- Identify bottlenecks across services
- Visualize service dependencies
- Root cause analysis for failures
```

### Centralized Logging
**Tools:** ELK Stack (Elasticsearch, Logstash, Kibana), Datadog, CloudWatch
```
Log levels:
- ERROR: Payment failed, inventory unavailable
- WARN: Retry attempts, slow queries
- INFO: Order created, payment processed
- DEBUG: Detailed transaction logs (sensitive, log only when needed)
```

## Implementation Roadmap

### Phase 1: MVP (Monolithic)
**Timeline:** 4-6 weeks
- Create Order, OrderItem entities with JPA
- REST API for order creation and status
- Payment integration (Stripe/PayPal)
- Simple inventory management
- Synchronous order processing

**Tech Stack:**
- Spring Boot 4.0.5
- PostgreSQL
- Spring Data JPA
- Spring Web
- Stripe/PayPal SDK

### Phase 2: Async Processing
**Timeline:** 2-3 weeks
- Add message queue (RabbitMQ/Kafka)
- Event-based order status updates
- Asynchronous payment processing
- Background fulfillment tasks
- Idempotency keys for retry safety

### Phase 3: Event Sourcing & Saga
**Timeline:** 4-5 weeks
- Implement Event Sourcing for Order aggregate
- Choreography-based Saga for fulfillment workflow
- Read models for analytics
- Event replay and recovery mechanisms

### Phase 4: Microservices Migration (Optional)
**Timeline:** 6-8 weeks
- Extract Payment Service
- Extract Inventory Service
- Extract Fulfillment Service
- Service discovery (Consul/Eureka)
- API Gateway (Kong/Spring Cloud Gateway)

### Phase 5: Observability & Optimization
**Timeline:** 3-4 weeks
- Distributed tracing implementation
- Metrics collection and dashboards
- Load testing and performance tuning
- Database optimization and sharding preparation

## Key Trade-Offs Summary

| Dimension | Option A | Option B | Recommendation |
|-----------|----------|----------|-----------------|
| **Architecture** | Monolithic | Microservices | Start Monolithic, Microservices after 100K orders/day |
| **Consistency** | Strong (ACID) | Eventual | Strong for Payments, Eventual for Status |
| **Scaling** | Vertical | Horizontal | Both, with horizontal for stateless services |
| **Data Store** | SQL (PostgreSQL) | NoSQL (MongoDB) | PostgreSQL + Redis cache |
| **Processing** | Synchronous | Asynchronous | Synchronous for checkout, Async for fulfillment |
| **Saga Pattern** | Choreography | Orchestration | Choreography for simple, Orchestration for complex |
| **Complexity** | Low | High | Start low, increase as needed |
| **Operational Overhead** | Minimal | Significant | Plan for DevOps infrastructure |
| **Event Storage** | Relational DB | Dedicated Event Store | Relational DB (Postgres with event table) initially |

## Non-Functional Requirements

### Performance
- Order creation: <500ms (p95)
- Order status retrieval: <100ms (cached)
- Payment processing: <2s (p95)
- Inventory check: <100ms (cached)

### Availability
- Target: 99.9% uptime (8.76 hours downtime/year)
- Graceful degradation: Serve stale cache if DB unavailable
- Failover: Active-active deployment across regions

### Scalability
- Support 1000 orders/second sustained
- Handle 10x burst traffic for 5 minutes
- Database growth: 1TB/year (with archival)

### Security
- TLS 1.2+ for all APIs
- OAuth 2.0 for API authentication
- Encrypt sensitive data (PII) at rest and in transit
- Rate limiting: 1000 requests/minute per customer
- Input validation: Prevent SQL injection, XSS, XXE

## Conclusion

This design balances multiple competing concerns:
- **Consistency vs. Scalability:** Use strong consistency where it matters (payments), eventual consistency elsewhere
- **Simplicity vs. Features:** Start monolithic with async processing, migrate to microservices when operational complexity is justified
- **Cost vs. Reliability:** Use caching and CDN to reduce DB load; implement redundancy for critical components
- **Speed vs. Durability:** Event sourcing provides audit trail at cost of storage and processing overhead

Choose implementation approach based on current business requirements, team expertise, and expected growth trajectory.


## Challenge - Brokage Order Service

Spring Boot service that manages customer assets and orders (BUY/SELL). Includes validation, layered architecture, and unit tests for service, processors, cancellation strategies, and controllers.

### Tech stack
- Java 22, Spring Boot 3.5.6
- Spring Web, Spring Data JPA
- H2 (runtime), Jakarta Validation
- Lombok (entities/DTOs/builders)
- JUnit 5, Mockito, AssertJ

### Project structure (high level)
- `controller/` HTTP endpoints (`OrderController`, `AssetController`)
- `service/` service interfaces (`OrderService`, `AssetService`) and strategies
- `service/impl/` concrete implementations (`OrderServiceImpl`, `AssetServiceImpl`, processors, strategies, managers)
- `entity/` JPA entities (`Order`, `Asset`)
- `repository/` Spring Data repositories (`OrderRepository`, `AssetRepository`)
- `dto/` request/response records (`CreateOrder`, `OrderResponse`, `AssetResponse`)
- `mapper/` mapping helpers (`OrderResponseMapper`, `AssetResponseMapper`)
- `common/` exception handling (`OrderApiExceptionHandler`, error response model)
- `audit/` audit system (`AuditLog`, `AuditService`, `AuditAspect`, `@Auditable`)

### Build & run
```bash
mvn clean package
mvn spring-boot:run
```

Default port: 8080

### API Documentation
**Swagger UI**: Interactive API documentation available at:
- `http://localhost:8080/swagger-ui/index.html`
- Browse all endpoints, test requests, and view response schemas
- Auto-generated from Spring Boot annotations

### API Testing
**Postman Collection**: Import the provided Postman collection for easy API testing
- Collection includes all endpoints with example requests/responses
- Pre-configured environment variables for different scenarios
- Test scenarios for success and error cases
- Audit log testing examples

### Run tests
```bash
mvn -q test
```

Focused runs:
```bash
mvn -q -Dtest=OrderServiceImplTest test
mvn -q -Dtest="*ProcessorTest" test
mvn -q -Dtest="*CancellationStrategyTest" test
mvn -q -Dtest=OrderControllerTest,AssetControllerTest test
mvn -q -Dtest=AuditServiceTest test
```

### Domain model
- `Order` fields: `id`, `customerId`, `assetName`, `orderSide (BUY|SELL)`, `size (Long)`, `price (BigDecimal)`, `status (PENDING|EXECUTED|CANCELLED)`, `createDate (Instant)`
- `Asset` fields: `id`, `customerId`, `assetName`, `size (Long)`, `usableSize (Long)`
- `AuditLog` fields: `id`, `operation`, `entityType`, `entityId`, `customerId`, `details`, `status (SUCCESS|FAILURE)`, `timestamp`, `errorMessage`

### REST API

#### Orders
- Create order
  - `POST /api/orders`
  - Request body:
    ```json
    {
      "customer": "cust1",
      "side": "BUY",
      "asset": "BTC",
      "size": 2,
      "price": 10.50
    }
    ```
  - Response body (`201 Created`):
    ```json
    {
      "customerId": "cust1",
      "asset": "BTC",
      "side": "BUY",
      "size": 2,
      "price": 10.50,
      "status": "PENDING",
      "createDate": "2024-01-01T00:00:00Z"
    }
    ```

- List orders
  - `GET /api/orders/list?customer=cust1&startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z`
  - Response: `List<OrderResponse>`

- Delete order
  - `DELETE /api/orders/{orderId}` → `204 No Content`
  - Only `PENDING` orders can be cancelled; cancellation refunds reserved balances.

#### Assets
- List assets by customer
  - `GET /api/asset/list?customerId=cust1`
  - Response body:
    ```json
    [
      {
        "customerId": "cust1",
        "assetName": "BTC",
        "totalSize": 10.00,
        "usableSize": 7.50
      }
    ]
    ```

### Business rules (summary)
- BUY order
  - Reserves TRY balance equal to `price * size` from `usableSize` of customer TRY asset
  - Fails if TRY asset missing or insufficient usable balance
- SELL order
  - Reserves `size` units of the asset from `usableSize`
  - Fails if asset missing or insufficient usable balance
- Cancellation refunds reserved amounts via side-specific strategy:
  - BUY: refund TRY amount (`price * size`)
  - SELL: refund reserved shares (`size`)

### Validation & errors
- Request validation via Jakarta Validation on `CreateOrder`
- Centralized error handling in `OrderApiExceptionHandler`
  - Validation errors → `400` with details
  - Domain errors (e.g., invalid customer/asset/order) → `400/404` depending on case
  - Unexpected errors → `500` with standardized error body

### Audit & Compliance
- **Automatic Audit Logging**: All `createOrder` and `deleteOrder` operations are automatically audited using AOP
- **Audit Data Captured**:
  - Operation type (`CREATE_ORDER`, `DELETE_ORDER`)
  - Entity details (order information, customer ID, asset details)
  - Success/failure status with error messages
  - Precise timestamps for compliance tracking
- **Audit Storage**: All audit data persisted in `audit_logs` table
- **AOP Implementation**: Uses `@Auditable` annotation and `AuditAspect` for non-intrusive audit logging

### Notes
- Security configuration may be omitted/disabled in this challenge scope
- Lombok must be enabled in your IDE/build for getters/builders
- AOP-based audit logging requires `spring-boot-starter-aop` dependency

### Useful Maven commands
```bash
# clean build
mvn clean verify

# run with tests skipped
mvn spring-boot:run -DskipTests

# format test selection
mvn -q -Dtest="*Test" test

# run specific audit tests
mvn -q -Dtest=AuditServiceTest test

# run all audit-related tests
mvn -q -Dtest="*Audit*" test
```

### Postman Collection
The project includes a comprehensive Postman collection with:

**Environment Variables:**
- `baseUrl`: http://localhost:8080
- `customerId`: cust1
- `orderId`: 1
- `startDate`: 2024-01-01T00:00:00Z
- `endDate`: 2024-12-31T23:59:59Z

**Collection Structure:**
- **Orders**
  - Create Order (BUY/SELL examples)
  - List Orders (with date filtering)
  - Delete Order
- **Assets**
  - List Assets by Customer

**Test Scenarios:**
- ✅ Success cases for all endpoints
- ❌ Error cases (validation, not found, insufficient balance)
- 📊 Data consistency checks

**Usage:**
1. Start the application (`mvn spring-boot:run`)
2. Access Swagger UI at `http://localhost:8080/swagger-ui/index.html`
3. Or import the Postman collection for testing
4. Set up environment variables in Postman
5. Run the collection or individual requests

### Audit System Architecture
- **AuditLog Entity**: JPA entity storing audit records with Lombok annotations
- **AuditService**: Service layer for creating audit log entries
- **AuditAspect**: AOP aspect intercepting `@Auditable` annotated methods
- **@Auditable Annotation**: Marks methods for automatic audit logging
- **AuditRepository**: Spring Data JPA repository for audit queries
- **Automatic Integration**: `createOrder` and `deleteOrder` methods automatically audited



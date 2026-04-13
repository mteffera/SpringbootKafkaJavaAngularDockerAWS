# Copilot Instructions for SpringBoot Kafka Java Angular Docker AWS

## Architecture Overview

This is a **distributed employee management system** with event-driven architecture:

- **Backend**: Spring Boot 3.2.5 with Kafka, H2 (dev) / PostgreSQL (prod), Circuit Breaker pattern
- **Frontend**: Angular 21 with standalone components, RxJS
- **Messaging**: Kafka for async employee creation events
- **Resilience**: Resilience4j circuit breakers on all critical service methods
- **Containerization**: Docker containers orchestrated via Docker Compose

**Key insight**: The system implements the Circuit Breaker pattern to prevent cascading failures. When database or Kafka operations fail, fallback methods return safe defaults (null or empty list) instead of propagating errors.

## Critical Developer Workflows

### Backend Build & Run (Spring Boot)
```bash
# Navigate to backend
cd demo/demo

# Build with Maven
./mvnw clean package

# Run tests
./mvnw test

# Run locally (starts on port 8080)
./mvnw spring-boot:run

# Docker build
docker build -f infra.docker/Dockerfile.backend -t employee-backend .
```

**Important**: Backend expects Kafka on `localhost:9092` (configured in `application.properties`). If Kafka is down, circuit breaker protection kicks in after 5 failed requests.

### Frontend Build & Run (Angular)
```bash
# Navigate to frontend
cd frontend/employee-ui

# Install dependencies
npm install

# Dev server (starts on port 4200)
npm start

# Build for production
npm build

# Run tests
npm test

# Docker build
docker build -t employee-frontend .
```

**Important**: Frontend makes API calls to `http://localhost:8080/api/employees` (hardcoded in `employee.ts` line 21). CORS is enabled with `@CrossOrigin(origins = "*")` on the controller.

### Full Stack with Docker Compose
```bash
# From workspace root
docker-compose -f demo/demo/infra.k8s/docker-compose.yml up
# Starts: Kafka, Zookeeper, Backend, Frontend, H2 DB console
```

## Project-Specific Conventions

### Database Layer (JPA/Hibernate)
- **ORM**: Spring Data JPA with Hibernate
- **Development DB**: H2 in-memory (auto-created on startup, cleared on shutdown)
- **Production DB**: PostgreSQL (switch via `application.properties`)
- **Schema Management**: `spring.jpa.hibernate.ddl-auto=create-drop` in dev
- **Key file**: `demo/demo/src/main/java/com/mekdes/demo/employee/Employee.java`

### Event Publishing Pattern
**File**: `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeEventPublisher.java`

```java
// Publishes to Kafka topic: "employee-created"
// Format: "{id},{firstName},{lastName}"
// This is a WRITE-ONLY operation (no return value used)
// Protected by circuit breaker: failureRateThreshold=50%, window=5 requests
```

### Circuit Breaker Configuration
**File**: `demo/demo/src/main/resources/application.properties` (lines ~38-48)

Three circuit breakers are configured identically:
- `employeeCreate`: Protects `EmployeeService.create()`
- `employeeList`: Protects `EmployeeService.findAll()`
- `kafkaPublisher`: Protects `EmployeeEventPublisher.publishEmployeeCreated()`

**Behavior**: 50% failure rate over 5 requests → opens circuit → calls fallback method → waits 10 seconds before testing recovery.

### Frontend Component Pattern
**File**: `frontend/employee-ui/src/app/employee/employee.ts`

Follows Angular standalone component pattern:
- Direct `HttpClient` injection (no service layer)
- Manual error handling in observable subscriptions
- `loading` and `error` state flags for UI feedback
- Form validation before HTTP POST

**Important**: Component hardcodes API URL. For multi-environment support, inject via config.

## Integration Points & External Dependencies

### Kafka Configuration
- **Bootstrap Server**: `localhost:9092` (configurable in `application.properties`)
- **Topic**: `employee-created` (auto-created by Kafka)
- **Consumer Group**: `employee-service-group`
- **Serialization**: String (not JSON)
- **Consumer**: `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeEventConsumer.java`

### API Endpoints
| Method | Endpoint | Behavior |
|--------|----------|----------|
| POST | `/api/employees` | Create + publish event + save to DB |
| GET | `/api/employees` | Fetch all from DB (no event publishing) |
| GET | `/actuator/health` | Health check (Spring Boot Actuator) |
| GET | `/h2-console` | H2 database browser (dev only) |

### CORS Configuration
Backend enables all origins: `@CrossOrigin(origins = "*")` on controller. Adjust in production.

## Common Tasks & Implementation Patterns

### Adding a New Employee Field
1. Modify `demo/demo/src/main/java/com/mekdes/demo/employee/Employee.java` - add JPA field
2. Update `frontend/employee-ui/src/app/employee/employee.ts` - add to Employee interface
3. Update `frontend/employee-ui/src/app/employee/employee.html` - add input field
4. H2 schema auto-migrates on restart (dev); manually migrate in prod

### Testing Circuit Breaker Behavior
```bash
# Simulate failure by stopping Kafka
docker stop <kafka-container>

# Make 6+ requests → 5th fails, 6th triggers circuit breaker (fallback called)
curl -X GET http://localhost:8080/api/employees

# Check circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# Restart Kafka to recover
docker start <kafka-container>
# After 10s wait-duration, circuit attempts recovery
```

### Local Development with All Services
```bash
# Start only Kafka + Zookeeper
docker-compose -f demo/demo/infra.k8s/docker-compose.yml up kafka zookeeper

# Run backend in IDE/terminal
cd demo/demo && ./mvnw spring-boot:run

# Run frontend in separate terminal
cd frontend/employee-ui && npm start

# Access: http://localhost:4200
```

## Key Files to Understand

| File | Purpose |
|------|---------|
| `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeService.java` | Core business logic + circuit breaker definitions |
| `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeController.java` | REST endpoints + CORS |
| `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeEventPublisher.java` | Kafka producer |
| `demo/demo/src/main/java/com/mekdes/demo/employee/EmployeeEventConsumer.java` | Kafka consumer + logging |
| `demo/demo/src/main/resources/application.properties` | Database, Kafka, Circuit Breaker config |
| `frontend/employee-ui/src/app/employee/employee.ts` | Angular component + HTTP client |
| `demo/demo/infra.k8s/docker-compose.yml` | Full stack orchestration |

---

**Last Updated**: April 2026  
**Status**: Production-ready with comprehensive error handling and resilience patterns

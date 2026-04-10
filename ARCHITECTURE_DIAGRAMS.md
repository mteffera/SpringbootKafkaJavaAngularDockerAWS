# System Architecture & Data Flow Diagrams

## 1. Overall Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          INTERNET / CLIENT BROWSER                  │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │   HTTP Requests (80)    │
                    │   Angular Frontend      │
                    │  http://localhost:4200  │
                    └────────────┬────────────┘
                                 │
                        REST API CALLS (CORS)
                                 │
                ┌────────────────┴────────────────────┐
                │                                     │
        ┌───────▼──────────┐              ┌──────────▼────────┐
        │  POST/GET        │              │   Static Assets   │
        │  /api/employees  │              │   index.html      │
        │                  │              │   styles.css      │
        │  Data in JSON    │              │   main.ts         │
        └───────┬──────────┘              └───────────────────┘
                │
        ┌───────▼───────────────────────────────────┐
        │  SPRING BOOT BACKEND - Port 8080          │
        │  ╔════════════════════════════════════╗   │
        │  ║  EmployeeController                ║   │
        │  ║  ├─ POST /api/employees            ║   │
        │  ║  └─ GET /api/employees             ║   │
        │  ╚════════════════════════════════════╝   │
        │          │                    │            │
        │          ▼                    ▼            │
        │  ┌──────────────────┐  ┌──────────────┐  │
        │  │ EmployeeService  │  │ CircuitBreaker   │
        │  │ @CircuitBreaker  │  │ (Resilience4j)  │
        │  │ - create()       │  │ - create        │
        │  │ - findAll()      │  │ - findAll       │
        │  └────────┬─────────┘  │ - kafkaPublish  │
        │           │             └─────┬──────────┘
        │           │                   │
        │    ┌──────┴───────┬───────────┤
        │    │              │           │
        │    ▼              ▼           │
        │ ┌──────────┐  ┌────────────┐ │
        │ │Repository│  │   Kafka    │ │
        │ │(JPA)     │  │  Producer  │ │
        │ └────┬─────┘  │            │ │
        │      │        └────┬───────┘ │
        │      │             │         │
        │      │    Topic: employee-created
        │      │    Message: id,firstName,lastName
        │      │             │
        │      │ ┌───────────┴─────────────┐
        │      │ │                         │
        │      ▼ ▼                         ▼
        │  ┌────────────────┐      ┌─────────────────┐
        │  │  H2 Database   │      │ Kafka Consumer  │
        │  │  (In-Memory)   │      │ Group: employee-│
        │  │  Table:        │      │ service-group   │
        │  │  EMPLOYEE      │      │                 │
        │  │ (id, first,    │      │ Logs & Processes│
        │  │  last, email)  │      │ Employee Events │
        │  └────────────────┘      └─────────────────┘
        │
        └──────────────────────────────────────────────┘
```

---

## 2. Request/Response Flow

### Add Employee Flow

```
User Browser (http://localhost:4200)
    │
    │ Enters: John, Doe, john@example.com
    │
    ▼
┌─────────────────────────────┐
│ EmployeeComponent           │
│ addEmployee()               │
│                             │
│ Validates input ✓           │
│ loading = true              │
└──────────┬──────────────────┘
           │
           │ HTTP POST
           │ /api/employees
           │ {"firstName":"John",...}
           │
           ▼
┌──────────────────────────────────────┐
│ EmployeeController                   │
│ @PostMapping                         │
│ create(@RequestBody Employee)        │
└──────────┬───────────────────────────┘
           │
           │ Calls service
           │
           ▼
┌──────────────────────────────────────┐
│ EmployeeService                      │
│ @CircuitBreaker(name="employeeCreate")
│ create(Employee employee)            │
└──────────┬───────────────────────────┘
           │
           ├─ Circuit Breaker Check
           │  ├─ If OPEN: Call fallback → return null
           │  ├─ If CLOSED: Continue
           │  └─ If HALF_OPEN: Test call
           │
           │ (Circuit is CLOSED - proceed)
           │
           ▼
┌──────────────────────────────────────┐
│ Save to Database                     │
│ repository.save(employee)            │
│                                      │
│ ↓ INSERT INTO EMPLOYEE               │
│   (FIRST_NAME, LAST_NAME, EMAIL)     │
│   VALUES ('John', 'Doe', ...)        │
│                                      │
│ Returns: Employee with id=1          │
└──────────┬───────────────────────────┘
           │
           ▼
┌──────────────────────────────────────┐
│ Publish Kafka Event                  │
│ eventPublisher.publishEmployeeCreated│
│                                      │
│ @CircuitBreaker(name="kafkaPublisher")
│ kafkaTemplate.send(topic, payload)   │
│                                      │
│ Topic: employee-created              │
│ Message: "1,John,Doe"                │
└──────────┬───────────────────────────┘
           │
           │ (Simultaneously consumed)
           │
           ├──────────────────┐
           │                  │
           ▼                  ▼
    Return to          Kafka Consumer
    Controller         EmployeeEventConsumer
           │           @KafkaListener
           │           Logs: "📥 Received..."
           │
           ▼
┌──────────────────────────────┐
│ HTTP Response 200 OK         │
│ {"id":1,"firstName":"John"...}
└──────────┬──────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ Frontend receives response   │
│ employees.push(emp)          │
│ newEmployee = {} (reset)     │
│ loading = false              │
│ alert("Employee added!")     │
└──────────┬──────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ UI Updates                   │
│ - Employee added to list     │
│ - Form cleared               │
│ - User sees success message  │
└──────────────────────────────┘
```

---

## 3. Get All Employees Flow

```
User Browser
    │
    │ Page Load or "Refresh" click
    │
    ▼
┌──────────────────────────────┐
│ ngOnInit() or loadEmployees()│
│ loading = true               │
│ error = null                 │
└──────────┬───────────────────┘
           │
           │ HTTP GET /api/employees
           │
           ▼
┌──────────────────────────────────────┐
│ EmployeeController                   │
│ @GetMapping                          │
│ list()                               │
└──────────┬───────────────────────────┘
           │
           │
           ▼
┌──────────────────────────────────────┐
│ EmployeeService                      │
│ @CircuitBreaker(name="employeeList") │
│ findAll()                            │
└──────────┬───────────────────────────┘
           │
           ├─ Circuit Breaker Check
           │  ├─ If OPEN: Call listFallback() → return []
           │  └─ If CLOSED: Continue
           │
           │ (Circuit is CLOSED - proceed)
           │
           ▼
┌──────────────────────────────────────┐
│ repository.findAll()                 │
│ SELECT * FROM EMPLOYEE               │
│                                      │
│ Returns: List<Employee>              │
│ [                                    │
│   {id:1, firstName:"John", ...},    │
│   {id:2, firstName:"Alice", ...}    │
│ ]                                    │
└──────────┬───────────────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ HTTP Response 200 OK         │
│ [employee1, employee2, ...]  │
└──────────┬──────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ Frontend receives list       │
│ employees = data             │
│ loading = false              │
└──────────┬──────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ UI displays employees        │
│ - Table with all employees   │
│ - Each row: id, first, last  │
│ - User can see all records   │
└──────────────────────────────┘
```

---

## 4. Circuit Breaker State Machine

```
                    ┌─────────────┐
                    │  CLOSED     │
                    │  (Normal)   │
                    │             │
                    │ Success ✓   │
                    │             │
                    └──────┬──────┘
                           │
                    Failure count increases
                    (Sliding window: 5 requests)
                           │
                    Failure rate > 50%?
                           │
                      ┌────┴─────────────────┐
                      │                      │
                     NO                     YES
                      │                      │
                      │                      ▼
                      │              ┌──────────────┐
                      │              │    OPEN      │
                      │              │  (Blocked)   │
                      │              │              │
                      │              │ All requests │
                      │              │ → Fallback   │
                      │              │ immediately  │
                      └──────────────┤              │
                                     │              │
                                     │ Wait 10 sec  │
                                     │ (timeout)    │
                                     │              │
                                     └──────┬───────┘
                                            │
                                            ▼
                                     ┌──────────────────┐
                                     │   HALF-OPEN      │
                                     │  (Testing)       │
                                     │                  │
                                     │ Allow 1 request  │
                                     │ to test recovery │
                                     │                  │
                                     └────┬─────────┬───┘
                                          │         │
                                       Success   Failure
                                          │         │
                                          │         │
                        ┌─────────────────┘         │
                        │                           │
                        ▼                           ▼
                   ┌──────────┐             ┌──────────────┐
                   │  CLOSED  │◄────────────│    OPEN      │
                   │          │   (Back to) │  (Retry)     │
                   └──────────┘             └──────────────┘
```

---

## 5. Kafka Event Processing

```
┌──────────────────────────────────────────────────────────────┐
│  EmployeeService.create()                                    │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Employee saved = repository.save(employee)           │ │
│  │  // Save returns: Employee{id: 1, ...}                │ │
│  └────────┬───────────────────────────────────────────────┘ │
│           │                                                  │
│           ▼                                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  eventPublisher.publishEmployeeCreated(saved)         │ │
│  │                                                        │ │
│  │  @CircuitBreaker(name="kafkaPublisher")               │ │
│  │  public void publishEmployeeCreated(Employee emp)     │ │
│  │  {                                                     │ │
│  │    String payload = emp.getId() + ","                │ │
│  │                  + emp.getFirstName() + ","          │ │
│  │                  + emp.getLastName();                │ │
│  │    // payload = "1,John,Doe"                          │ │
│  │                                                        │ │
│  │    kafkaTemplate.send(TOPIC, payload);               │ │
│  │    // Send to: "employee-created"                     │ │
│  │  }                                                     │ │
│  └────────┬───────────────────────────────────────────────┘ │
│           │                                                  │
└───────────┼──────────────────────────────────────────────────┘
            │
            ▼
    ┌─────────────────────┐
    │   KAFKA BROKER      │
    │  Topic: employee-   │
    │  created            │
    │                     │
    │  Partition 0        │
    │  Message: {         │
    │    key: null,       │
    │    value: "1,John,  │
    │    Doe"             │
    │  }                  │
    │                     │
    │  Offset: 0          │
    └────────┬────────────┘
             │
             │
    ┌────────▼────────────────────────────────┐
    │  KAFKA CONSUMER                         │
    │  Group: employee-service-group          │
    │                                         │
    │  EmployeeEventConsumer                  │
    │  @KafkaListener(                        │
    │    topics = "employee-created",         │
    │    groupId = "employee-service-group"   │
    │  )                                      │
    │  public void consumeMessage(             │
    │    String message                       │
    │  ) {                                    │
    │    log.info("📥 Received Kafka         │
    │    message: {}", message);              │
    │    // Logs: "📥 Received Kafka         │
    │    // message: 1,John,Doe"             │
    │  }                                      │
    │                                         │
    │  Offset: 0 (latest consumed)            │
    └─────────────────────────────────────────┘
```

---

## 6. Database Schema

```
┌─────────────────────────────────────────┐
│  H2 DATABASE (jdbc:h2:mem:testdb)       │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ EMPLOYEE TABLE                  │   │
│  │ ┌─────────────────────────────┐ │   │
│  │ │ ID (BIGINT, PK, AUTO)      │ │   │
│  │ │ FIRST_NAME (VARCHAR(255))   │ │   │
│  │ │ LAST_NAME (VARCHAR(255))    │ │   │
│  │ │ EMAIL (VARCHAR(255))        │ │   │
│  │ └─────────────────────────────┘ │   │
│  │                                 │   │
│  │ Sample Data:                    │   │
│  │ ┌─────────────────────────────┐ │   │
│  │ │ ID │ FIRST_NAME │ LAST_NAME │ │   │
│  │ ├─────────────────────────────┤ │   │
│  │ │ 1  │ John       │ Doe       │ │   │
│  │ │ 2  │ Alice      │ Smith     │ │   │
│  │ │ 3  │ Bob        │ Johnson   │ │   │
│  │ └─────────────────────────────┘ │   │
│  └─────────────────────────────────┘   │
│                                         │
│  Access: http://localhost:8080/        │
│          h2-console                     │
│  URL: jdbc:h2:mem:testdb               │
│  User: sa                               │
│  Pass: (empty)                          │
└─────────────────────────────────────────┘
```

---

## 7. Docker Container Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  DOCKER COMPOSE NETWORK (default bridge)                     │
│                                                              │
│  ┌─────────────────┐                                        │
│  │ Zookeeper       │                                        │
│  │ Container       │                                        │
│  │                 │                                        │
│  │ Port: 2181      │                                        │
│  │ Service: zk     │                                        │
│  └────────┬────────┘                                        │
│           │                                                 │
│           │ Coordinates                                     │
│           │                                                 │
│  ┌────────▼──────────────────┐                             │
│  │ Kafka                      │                             │
│  │ Container                  │                             │
│  │                            │                             │
│  │ Port: 9092                 │                             │
│  │ Service: kafka             │                             │
│  │ Env:                       │                             │
│  │  - KAFKA_BROKER_ID=1       │                             │
│  │  - KAFKA_ZOOKEEPER_        │                             │
│  │    CONNECT=zookeeper:2181  │                             │
│  │                            │                             │
│  │ Depends on: zookeeper      │                             │
│  │ Health: Custom check       │                             │
│  └────────┬───────────────────┘                             │
│           │                                                 │
│           │ Connects                                        │
│           │                                                 │
│  ┌────────▼─────────────────────────────┐                  │
│  │ Backend (Spring Boot)                 │                  │
│  │ Container                             │                  │
│  │                                       │                  │
│  │ Port: 8080                            │                  │
│  │ Service: backend                      │                  │
│  │ Env:                                  │                  │
│  │  - SPRING_KAFKA_BOOTSTRAP_SERVERS    │                  │
│  │    =kafka:9092                        │                  │
│  │                                       │                  │
│  │ Depends on: kafka (healthy)           │                  │
│  │ Health: HTTP /actuator/health         │                  │
│  └────────┬──────────────────────────────┘                  │
│           │                                                 │
│           │ REST API                                        │
│           │                                                 │
│  ┌────────▼────────────────────────────┐                   │
│  │ Frontend (Angular)                   │                   │
│  │ Container                            │                   │
│  │                                      │                   │
│  │ Port: 4200                           │                   │
│  │ Service: frontend                    │                   │
│  │ Env:                                 │                   │
│  │  - BACKEND_URL=http://backend:8080   │                   │
│  │                                      │                   │
│  │ Depends on: backend (healthy)        │                   │
│  └──────────────────────────────────────┘                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 8. Error Handling & Circuit Breaker

```
Normal Path:
Request → Service → [Circuit: CLOSED] → Database → Response

Failure Path - Circuit Opens:
Request → Service → [Circuit: OPEN] → Fallback Method → Immediate Response

Recovery Path - Half-Open:
Request → Service → [Circuit: HALF_OPEN] → Test Request → DB
                                            ├─ Success? → Close Circuit
                                            └─ Failure? → Reopen Circuit

EmployeeService Fallbacks:

1. createFallback()
   Input: Employee, Throwable
   Output: null
   Message: "❌ Create employee failed, circuit breaker opened"

2. listFallback()
   Input: Throwable
   Output: List.of() (empty list)
   Message: "❌ List employees failed, circuit breaker opened"

3. Kafka Publisher Fallback
   Input: Employee, Throwable
   Output: void (silent failure)
   Message: "Kafka publish failed, circuit breaker opened"
```

---

## 9. Deployment Sequence Diagram

```
Time Flow (Docker Compose Up):

t=0s    Start docker-compose up --build
        ↓
        Build backend Docker image (Maven compile)
        Build frontend Docker image (npm build)
        ↓
t=30s   Create and start zookeeper container
        ↓
        zookeeper ready (port 2181 listening)
        ↓
t=35s   Create and start kafka container
        ↓
        Kafka connects to zookeeper
        ↓
t=45s   Kafka health check: PASS ✓
        ↓
        Create and start backend container
        ↓
        Backend starts Spring Boot application
        ↓
t=60s   Backend health check: http://localhost:8080/actuator/health
        ↓
t=65s   Health check: PASS ✓
        ↓
        Create and start frontend container
        ↓
        Frontend serves Angular app on port 4200
        ↓
t=75s   All services running ✓
        ↓
        Frontend: http://localhost:4200
        Backend API: http://localhost:8080
        Kafka: localhost:9092
        Zookeeper: localhost:2181
```

---

## 10. CORS Request Flow

```
Browser (http://localhost:4200)
    │
    │ Try to access: http://localhost:8080/api/employees
    │
    ▼
┌────────────────────────────────┐
│ Browser CORS Preflight Check   │
│                                │
│ OPTIONS /api/employees         │
│                                │
│ Headers:                       │
│  - Origin: http://localhost:4200
│  - Access-Control-Request-    │
│    Method: GET                 │
└────────┬───────────────────────┘
         │
         │ CORS Preflight Request
         │
         ▼
┌────────────────────────────────┐
│ Spring Boot CORS Configuration │
│                                │
│ @Configuration                 │
│ public class DemoApplication { │
│   @Bean                        │
│   WebMvcConfigurer corsConfig()│
│   {                            │
│     registry.addMapping(       │
│       "/api/**"                │
│     )                          │
│     .allowedOrigins("*")       │
│     .allowedMethods("*")       │
│     .allowedHeaders("*");      │
│   }                            │
│ }                              │
└────────┬───────────────────────┘
         │
         │ CORS Preflight Response
         │
         ▼
┌────────────────────────────────┐
│ Browser receives 200 OK        │
│ Headers:                       │
│  - Access-Control-Allow-Origin│
│    : *                         │
│  - Access-Control-Allow-Methods
│    : *                         │
│  - Access-Control-Allow-Headers
│    : *                         │
└────────┬───────────────────────┘
         │
         │ Preflight OK ✓
         │ Proceed with actual request
         │
         ▼
┌────────────────────────────────┐
│ Actual CORS Request            │
│ GET /api/employees             │
│ Origin: http://localhost:4200  │
└────────┬───────────────────────┘
         │
         ▼
┌────────────────────────────────┐
│ Backend Response               │
│ 200 OK                         │
│ [employee1, employee2, ...]    │
│ Headers:                       │
│  - Access-Control-Allow-Origin│
│    : *                         │
└────────┬───────────────────────┘
         │
         ▼
┌────────────────────────────────┐
│ Browser receives response      │
│ CORS check: PASS ✓             │
│ Data available to Angular      │
│ Component updates UI           │
└────────────────────────────────┘
```

---

These diagrams visualize the complete system architecture and data flow!

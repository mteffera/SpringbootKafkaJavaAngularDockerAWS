# Full Stack Setup: Employee Management Application

This document explains how to run both the frontend and backend applications together.

## Prerequisites

- Docker & Docker Compose installed
- Node.js 20+ (for local development)
- Java 17+ (for local development)
- Maven 3.8+ (for local development)

---

## Option 1: Run with Docker Compose (Recommended for Full Stack Testing)

### Step 1: Navigate to the project root
```bash
cd C:\Users\DELL\Desktop\ComputerScience\AWS\application code\demo\demo
```

### Step 2: Start all services with Docker Compose
```bash
docker-compose -f infra.k8s/docker-compose.yml up --build
```

**What this does:**
- Starts Zookeeper (Kafka dependency)
- Starts Kafka broker
- Builds and starts the backend Spring Boot application on port 8080
- Builds and starts the Angular frontend on port 4200

### Step 3: Access the application
- **Frontend:** Open browser and go to `http://localhost:4200`
- **Backend API:** `http://localhost:8080/api/employees`
- **H2 Console:** `http://localhost:8080/h2-console`

### Step 4: Stop the services
```bash
docker-compose -f infra.k8s/docker-compose.yml down
```

---

## Option 2: Run Backend and Frontend Locally

### Backend Setup

#### Step 1: Install backend dependencies
```bash
cd C:\Users\DELL\Desktop\ComputerScience\AWS\application code\demo\demo
mvn clean install
```

#### Step 2: Start Kafka locally (using Docker only)
```bash
docker-compose -f infra.k8s/docker-compose.yml up zookeeper kafka
```

#### Step 3: Start the backend application
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup (in a new terminal)

#### Step 1: Install frontend dependencies
```bash
cd C:\Users\DELL\Desktop\ComputerScience\AWS\application code\frontend\employee-ui
npm install
```

#### Step 2: Start the development server
```bash
npm start
```

The frontend will start on `http://localhost:4200`

---

## Application Features

### Backend API Endpoints

#### 1. Add New Employee
```http
POST /api/employees
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

#### 2. Get All Employees
```http
GET /api/employees
```

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }
]
```

### Integrated Technologies

#### 1. **Circuit Breaker (Resilience4j)**
- Protects `create()` and `findAll()` methods
- **Settings:**
  - Sliding window size: 5 requests
  - Failure threshold: 50%
  - Wait duration: 10 seconds
- Fallback methods return null or empty list when circuit is open

#### 2. **Kafka Producer & Consumer**
- **Topic:** `employee-created`
- **Producer:** Sends employee creation events to Kafka
- **Consumer:** Listens on consumer group `employee-service-group`
- **Message Format:** `{id},{firstName},{lastName}`

#### 3. **Database**
- **Default:** H2 in-memory database (for development/testing)
- **Production:** PostgreSQL (configured in application.properties)
- **Auto Schema:** Hibernate creates tables on startup

#### 4. **CORS Configuration**
- Allows all origins for `/api/**` endpoints
- Frontend can communicate with backend

---

## Testing the Application

### 1. Add an Employee
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Alice","lastName":"Smith","email":"alice@example.com"}'
```

### 2. Get All Employees
```bash
curl http://localhost:8080/api/employees
```

### 3. View Kafka Messages (if you have Kafka tools installed)
```bash
# In a new terminal, consume messages from the employee-created topic
kafka-console-consumer.sh --bootstrap-servers localhost:9092 \
  --topic employee-created --from-beginning
```

---

## Troubleshooting

### Backend won't start
- Check if port 8080 is already in use: `netstat -ano | findstr :8080`
- Ensure H2 database is enabled in `application.properties`

### Frontend can't connect to backend
- Ensure backend is running on port 8080
- Check CORS is enabled (should be by default)
- Check browser console for specific error messages

### Kafka connection issues
- Ensure Kafka container is running: `docker ps | grep kafka`
- Check Kafka logs: `docker logs <kafka-container-id>`

### Docker Compose issues
- Rebuild containers: `docker-compose down && docker-compose up --build`
- Check individual service logs: `docker-compose logs -f <service-name>`

---

## Project Structure

```
demo/
├── src/main/java/com/mekdes/demo/
│   ├── DemoApplication.java           # Spring Boot entry point
│   └── employee/
│       ├── Employee.java              # JPA Entity
│       ├── EmployeeController.java    # REST endpoints
│       ├── EmployeeService.java       # Business logic (with circuit breaker)
│       ├── EmployeeRepository.java    # Data access
│       ├── EmployeeEventPublisher.java # Kafka producer
│       └── EmployeeEventConsumer.java  # Kafka consumer
├── src/main/resources/
│   └── application.properties         # Configuration
└── pom.xml                            # Maven dependencies

frontend/employee-ui/
├── src/
│   ├── app/
│   │   ├── app.config.ts             # Angular config (providers)
│   │   └── employee/
│   │       ├── employee.ts           # Component logic
│   │       ├── employee.html         # Template
│   │       └── employee.css          # Styles
│   └── main.ts                       # Bootstrap
└── package.json                      # NPM dependencies
```

---

## Key Improvements Made

1. ✅ Added **Circuit Breaker** to both create and list operations
2. ✅ Added **Kafka Consumer** configuration with proper group ID
3. ✅ Updated **Spring Boot** configuration with H2 and proper Kafka setup
4. ✅ Added **CORS** configuration to backend
5. ✅ Enhanced **Frontend** with error handling and loading states
6. ✅ Created **Docker Compose** file for containerized deployment
7. ✅ Added **HTTP Client** provider to Angular config
8. ✅ Added **Health checks** in Docker Compose for service dependencies

---

## Next Steps

- Add authentication (JWT)
- Implement pagination for employee list
- Add delete/update endpoints
- Add unit tests
- Deploy to Kubernetes using the YAML files in `infra.k8s/`

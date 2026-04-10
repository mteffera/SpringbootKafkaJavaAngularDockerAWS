# API Testing Guide

## Using cURL to Test the Backend

### 1. Add a New Employee

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

---

### 2. Get All Employees

```bash
curl http://localhost:8080/api/employees
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  },
  {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com"
  }
]
```

---

## Using Postman

### Import Collection

1. Open Postman
2. Click **Import**
3. Choose **Raw Text**
4. Paste the collection below:

```json
{
  "info": {
    "name": "Employee Management API",
    "description": "API collection for Employee Management Application",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Add Employee",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"email\": \"john.doe@example.com\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/employees",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "employees"]
        }
      }
    },
    {
      "name": "Get All Employees",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/employees",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "employees"]
        }
      }
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/actuator/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["actuator", "health"]
        }
      }
    },
    {
      "name": "Circuit Breaker Metrics",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/actuator/circuitbreakers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["actuator", "circuitbreakers"]
        }
      }
    }
  ]
}
```

---

## Testing with PowerShell (Windows)

### 1. Add Employee
```powershell
$body = @{
    firstName = "Alice"
    lastName = "Johnson"
    email = "alice.johnson@example.com"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/employees" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

### 2. Get All Employees
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/employees" `
  -Method GET
```

---

## Testing with JavaScript/Node.js

```javascript
// Using fetch API (works in browser console too)

// Add Employee
fetch('http://localhost:8080/api/employees', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    firstName: 'Bob',
    lastName: 'Wilson',
    email: 'bob.wilson@example.com'
  })
})
.then(response => response.json())
.then(data => console.log('Added:', data))
.catch(error => console.error('Error:', error));

// Get All Employees
fetch('http://localhost:8080/api/employees')
  .then(response => response.json())
  .then(data => console.log('Employees:', data))
  .catch(error => console.error('Error:', error));
```

---

## Testing Health & Metrics

### 1. Application Health
```bash
curl http://localhost:8080/actuator/health
```

### 2. Circuit Breaker Status
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

### 3. Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

---

## H2 Database Console

1. Open browser: `http://localhost:8080/h2-console`
2. Connection string: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: (leave blank)
5. Click **Connect**

### Useful SQL Queries

```sql
-- View all employees
SELECT * FROM EMPLOYEE;

-- Count employees
SELECT COUNT(*) FROM EMPLOYEE;

-- View table structure
DESCRIBE EMPLOYEE;

-- Insert test data directly
INSERT INTO EMPLOYEE (FIRST_NAME, LAST_NAME, EMAIL) 
VALUES ('Test User', 'Test', 'test@example.com');
```

---

## Testing Kafka Messages

### Using Kafka Console Consumer (if Kafka tools are installed)

```bash
# Start consuming messages from employee-created topic
kafka-console-consumer.sh \
  --bootstrap-servers localhost:9092 \
  --topic employee-created \
  --from-beginning
```

### View Topic Details
```bash
kafka-topics.sh \
  --bootstrap-servers localhost:9092 \
  --describe \
  --topic employee-created
```

### View Consumer Group Details
```bash
kafka-consumer-groups.sh \
  --bootstrap-servers localhost:9092 \
  --group employee-service-group \
  --describe
```

---

## Testing Circuit Breaker Failure Scenario

### 1. Stop the backend service
```bash
# Kill the spring boot process
# This will cause circuit breaker to open after several failed requests
```

### 2. Try making requests
```bash
# First few requests will fail quickly
curl http://localhost:8080/api/employees

# After threshold, circuit opens and returns fallback immediately
curl http://localhost:8080/api/employees
```

### 3. Check circuit breaker status
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

### 4. Restart the backend
```bash
mvn spring-boot:run
# or
docker-compose up backend
```

After wait duration (10 seconds by default), the circuit breaker will half-open and start accepting requests again.

---

## Browser Console Testing

Open browser developer tools (F12) and run in Console:

```javascript
// Add multiple employees
async function addEmployees() {
  const employees = [
    { firstName: 'Emma', lastName: 'Brown', email: 'emma@example.com' },
    { firstName: 'Michael', lastName: 'Davis', email: 'michael@example.com' },
    { firstName: 'Sarah', lastName: 'Miller', email: 'sarah@example.com' }
  ];

  for (const emp of employees) {
    try {
      const response = await fetch('http://localhost:8080/api/employees', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(emp)
      });
      const data = await response.json();
      console.log('Added:', data);
    } catch (error) {
      console.error('Error:', error);
    }
  }
}

// List all employees
async function listEmployees() {
  try {
    const response = await fetch('http://localhost:8080/api/employees');
    const data = await response.json();
    console.table(data);
  } catch (error) {
    console.error('Error:', error);
  }
}

// Run functions
addEmployees().then(() => listEmployees());
```

---

## Expected Outcomes

### Success Scenario
1. ✅ Frontend loads on http://localhost:4200
2. ✅ Backend responds on http://localhost:8080
3. ✅ Can add employees through UI or API
4. ✅ Can view all employees
5. ✅ Kafka receives messages (visible in logs)
6. ✅ H2 database contains employee records

### Circuit Breaker Scenario
1. ✅ Requests succeed normally
2. ⚠️ Service fails or times out
3. 🔴 Circuit breaker opens after threshold
4. ⚡ Fallback method returns immediately (empty list or null)
5. ⏳ Circuit waits 10 seconds in open state
6. 🟡 Circuit attempts half-open state
7. ✅ Circuit closes and requests succeed again

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Connection refused on port 8080 | Backend not running or port in use |
| CORS errors in browser | Check CORS configuration in DemoApplication |
| Kafka connection timeout | Ensure Kafka container is running |
| H2 console won't load | Check application.properties `spring.h2.console.enabled=true` |
| Circuit breaker not opening | Verify failure threshold is being reached |

---

## Performance Testing with Apache Bench

```bash
# Install Apache Bench (ab)
# Windows: choco install apache-httpd
# Mac: brew install httpd
# Linux: sudo apt-get install apache2-utils

# Load test the GET endpoint
ab -n 100 -c 10 http://localhost:8080/api/employees

# Load test the POST endpoint
ab -n 50 -c 5 -p employee.json -T application/json http://localhost:8080/api/employees
```

Where `employee.json` contains:
```json
{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@example.com"
}
```

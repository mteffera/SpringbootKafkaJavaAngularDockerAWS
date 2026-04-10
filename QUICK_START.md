# 🚀 QUICK START GUIDE

## 5-Minute Startup

### Prerequisites Check
```bash
docker --version       # Should show: Docker version 20.10.x
java -version         # Should show: Java 17+
mvn -version          # Should show: Maven 3.8+
node --version        # Should show: Node 20+
```

---

## ⚡ Start Full Stack (Fastest)

**Windows:**
```bash
cd C:\Users\DELL\Desktop\ComputerScience\AWS\application code
quickstart.bat
# Select option 1
```

**Linux/Mac:**
```bash
cd /path/to/project
chmod +x quickstart.sh
./quickstart.sh
# Select option 1
```

**Manual:**
```bash
cd demo
docker-compose -f infra.k8s/docker-compose.yml up --build
```

Wait 30-60 seconds for all services to start...

### Access the Application
- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api/employees
- **H2 Console:** http://localhost:8080/h2-console
- **Health Check:** http://localhost:8080/actuator/health

---

## 🧪 Quick Tests

### Test 1: Add Employee (via Frontend)
1. Go to http://localhost:4200
2. Enter employee details:
   - First Name: John
   - Last Name: Doe
   - Email: john@example.com
3. Click "Add Employee"
4. See it appear in the list below

### Test 2: Get All Employees (via API)
```bash
curl http://localhost:8080/api/employees
```

### Test 3: Add Employee (via API)
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Alice","lastName":"Smith","email":"alice@example.com"}'
```

---

## 📊 Service Status

All services should show healthy:

```
✅ Zookeeper (2181)
✅ Kafka (9092)
✅ Backend (8080)
✅ Frontend (4200)
```

Check with:
```bash
docker-compose ps
```

---

## 🛑 Stop Everything

```bash
docker-compose down
```

Or press `Ctrl+C` in the terminal where services are running.

---

## 📚 Detailed Documentation

- **Full Setup:** See [SETUP_AND_RUN.md](SETUP_AND_RUN.md)
- **Changes Made:** See [CHANGES_SUMMARY.md](CHANGES_SUMMARY.md)
- **API Testing:** See [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md)
- **Full Info:** See [README.md](README.md)

---

## 🆘 Troubleshooting

### Services won't start
```bash
# Rebuild containers
docker-compose down
docker-compose up --build

# Check logs
docker-compose logs -f
```

### Port 8080 in use
```bash
# Find and kill process on Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# On Mac/Linux
lsof -i :8080
kill -9 <PID>
```

### Frontend won't connect to backend
1. Check backend is running: http://localhost:8080/actuator/health
2. Clear browser cache (Ctrl+Shift+Delete)
3. Check browser console for errors (F12)

---

## 🎯 What's Included

✅ **Backend Features:**
- REST API (POST/GET)
- Circuit Breaker Protection
- Kafka Event Publishing
- Automatic Database Schema
- H2 In-Memory Database

✅ **Frontend Features:**
- Angular 21 Application
- Employee Add Form
- Employee List Display
- Error Handling
- Loading States

✅ **Infrastructure:**
- Docker Containerization
- Docker Compose Orchestration
- Kafka Event Bus
- Complete Health Checks

---

## 📱 API Quick Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/employees` | List all employees |
| POST | `/api/employees` | Add new employee |
| GET | `/actuator/health` | Check application health |
| GET | `/actuator/circuitbreakers` | View circuit breaker status |
| GET | `/h2-console` | Access database console |

---

## 🔄 Circuit Breaker Behavior

**Normal:** ✅ All requests succeed

**Failure Detected:** ⚠️ After 50% failure rate (threshold)

**Circuit Open:** 🔴 Returns fallback immediately

**Wait Duration:** ⏳ 10 seconds

**Half-Open:** 🟡 Attempts recovery

**Closed:** ✅ Normal operation resumed

---

## 📝 Employee Data Format

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

---

## 🆔 Kafka Topic

**Topic Name:** `employee-created`
**Consumer Group:** `employee-service-group`
**Message Format:** `{id},{firstName},{lastName}`

Example message:
```
1,John,Doe
2,Alice,Smith
```

---

## ✨ What Was Fixed

- ✅ Added Circuit Breaker to service methods
- ✅ Added Kafka consumer group configuration
- ✅ Added CORS support
- ✅ Added error handling to frontend
- ✅ Added HttpClient provider to Angular
- ✅ Added H2 database support
- ✅ Added Docker Compose setup
- ✅ Added resilience4j configuration
- ✅ Added comprehensive documentation

---

## 🎓 Learn More

Files to review:
1. `EmployeeService.java` - Circuit breaker implementation
2. `EmployeeController.java` - REST endpoints
3. `EmployeeEventPublisher.java` - Kafka producer
4. `EmployeeEventConsumer.java` - Kafka consumer
5. `employee.ts` - Frontend component

---

## 🎉 You're Ready!

Your full-stack application is configured and ready to run!

**Next Step:** Run `docker-compose up --build` and open http://localhost:4200

Enjoy! 🚀

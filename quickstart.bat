@echo off
REM Quick Start Script for Employee Management Application
REM Run this from the project root directory

echo.
echo ============================================
echo Employee Management Application - Quick Start
echo ============================================
echo.

echo Checking Docker installation...
docker --version > nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop first: https://www.docker.com/products/docker-desktop
    exit /b 1
)

echo Docker found!
echo.

echo What would you like to do?
echo 1. Run Full Stack with Docker Compose (Backend + Frontend + Kafka)
echo 2. Run Backend Only (Local) - Requires: Java 17+, Maven
echo 3. Run Frontend Only (Local) - Requires: Node.js 20+
echo 4. Stop all Docker services
echo.

set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" (
    echo.
    echo Starting Full Stack with Docker Compose...
    echo.
    cd demo
    docker-compose -f infra.k8s/docker-compose.yml up --build
    
) else if "%choice%"=="2" (
    echo.
    echo Starting Backend Service (Kafka must be running separately)...
    echo.
    echo Starting Kafka in Docker...
    cd demo
    docker-compose -f infra.k8s/docker-compose.yml up zookeeper kafka -d
    echo.
    echo Kafka started! Now starting backend...
    mvn spring-boot:run
    
) else if "%choice%"=="3" (
    echo.
    echo Starting Frontend (Backend must be running on http://localhost:8080)...
    echo.
    cd frontend\employee-ui
    npm install
    npm start
    
) else if "%choice%"=="4" (
    echo.
    echo Stopping all Docker services...
    cd demo
    docker-compose -f infra.k8s/docker-compose.yml down
    echo Done!
    
) else (
    echo Invalid choice. Please run the script again.
    exit /b 1
)

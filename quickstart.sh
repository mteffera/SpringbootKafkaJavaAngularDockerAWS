#!/bin/bash

# Quick Start Script for Employee Management Application
# Run this from the project root directory

echo ""
echo "============================================"
echo "Employee Management Application - Quick Start"
echo "============================================"
echo ""

# Check Docker installation
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH"
    echo "Please install Docker first: https://www.docker.com/products/docker-desktop"
    exit 1
fi

echo "Docker found!"
echo ""

echo "What would you like to do?"
echo "1. Run Full Stack with Docker Compose (Backend + Frontend + Kafka)"
echo "2. Run Backend Only (Local) - Requires: Java 17+, Maven"
echo "3. Run Frontend Only (Local) - Requires: Node.js 20+"
echo "4. Stop all Docker services"
echo ""

read -p "Enter your choice (1-4): " choice

case $choice in
    1)
        echo ""
        echo "Starting Full Stack with Docker Compose..."
        echo ""
        cd demo
        docker-compose -f infra.k8s/docker-compose.yml up --build
        ;;
    2)
        echo ""
        echo "Starting Backend Service (Kafka must be running separately)..."
        echo ""
        echo "Starting Kafka in Docker..."
        cd demo
        docker-compose -f infra.k8s/docker-compose.yml up zookeeper kafka -d
        echo ""
        echo "Kafka started! Now starting backend..."
        mvn spring-boot:run
        ;;
    3)
        echo ""
        echo "Starting Frontend (Backend must be running on http://localhost:8080)..."
        echo ""
        cd frontend/employee-ui
        npm install
        npm start
        ;;
    4)
        echo ""
        echo "Stopping all Docker services..."
        cd demo
        docker-compose -f infra.k8s/docker-compose.yml down
        echo "Done!"
        ;;
    *)
        echo "Invalid choice. Please run the script again."
        exit 1
        ;;
esac

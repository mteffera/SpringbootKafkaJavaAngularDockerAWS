The full source code for this project is available on GitHub:
https://github.com/mteffera/SpringbootKafkaJavaAngularDockerAWS.git

This repository contains two main components:

Backend (demo) — A Spring Boot application that includes REST APIs, Kafka producer, and Kafka consumer.

Frontend — An Angular application that interacts with the backend services.

The entire application can be deployed using Docker Compose, which orchestrates the backend, frontend, Kafka, Zookeeper, and supporting services in a single multi‑container environment.


Technologies I used in this project

1. Java(Version 17) + Spring Boot(Version 3.2.5) for building a clean, modular backend service with REST APIs

2. Apache Kafka for event‑driven communication, producers/consumers, and real‑time processing

3. Zookeeper for Kafka coordination and cluster stability

4. Angular(Version 21) for a responsive, modern frontend UI

5. Docker & Docker Compose to containerize the full stack and orchestrate multi‑service environments

6. Kafdrop for Kafka topic inspection and message visibility

7. H2 Database for lightweight, in‑memory persistence during development

8. Resilience4j for circuit breakers and fault‑tolerant service behavior

9. Distributed, containerized architecture demonstrating real‑world deployment patterns

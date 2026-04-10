package com.mekdes.demo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
@RequiredArgsConstructor
public class EmployeeEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "employee-created";

    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "fallbackPublish")
    public void publishEmployeeCreated(Employee employee) {
        String payload = employee.getId() + "," + employee.getFirstName() + "," + employee.getLastName();
        kafkaTemplate.send(TOPIC, payload);
    }

    public void fallbackPublish(Employee employee, Throwable t) {
        // log or store failed events; for demo, just log
        System.out.println("Kafka publish failed, circuit breaker opened: " + t.getMessage());
    }
}

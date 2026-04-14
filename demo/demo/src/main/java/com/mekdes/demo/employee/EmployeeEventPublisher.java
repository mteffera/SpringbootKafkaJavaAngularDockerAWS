package com.mekdes.demo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
@RequiredArgsConstructor
public class EmployeeEventPublisher {
    /**
     * I am using KafkaTemplate to publish messages to Kafka. 
     * It abstracts complexities of working with the KafkaProducer API. Spring aoutomattically handles
        Creating a ProducerRecord
        Serializing the key/value
        Managing the producer lifecycle
        Handling retries and acknowledgments
        Handling async futures
        Thread safety
        Connection pooling
        Error handling
     */
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "employee-created";

    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "fallbackPublish")
    public void publishEmployeeCreated(Employee employee) {
        String payload = employee.getId() + "," + employee.getFirstName() + "," + employee.getLastName();
        kafkaTemplate.send(TOPIC, payload);
    }

    public void fallbackPublish(Employee employee, Throwable t) {
        System.out.println("Kafka publish failed, circuit breaker opened: " + t.getMessage());
    }
}

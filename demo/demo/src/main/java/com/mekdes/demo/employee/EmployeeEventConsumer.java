package com.mekdes.demo.employee;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeeEventConsumer {
    @KafkaListener(topics = "employee-created", groupId = "employee-service-group")
    public void consumeMessage(String message) {
        log.info("Received Kafka message: {}", message);
    }
}

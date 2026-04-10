package com.mekdes.demo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final EmployeeEventPublisher eventPublisher;

    @CircuitBreaker(name = "employeeCreate", fallbackMethod = "createFallback")
    public Employee create(Employee employee) {
        Employee saved = repository.save(employee);
        eventPublisher.publishEmployeeCreated(saved);
        return saved;
    }

    @CircuitBreaker(name = "employeeList", fallbackMethod = "listFallback")
    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Employee createFallback(Employee employee, Throwable t) {
        System.err.println("❌ Create employee failed, circuit breaker opened: " + t.getMessage());
        return null;
    }

    public List<Employee> listFallback(Throwable t) {
        System.err.println("❌ List employees failed, circuit breaker opened: " + t.getMessage());
        return List.of();
    }
}

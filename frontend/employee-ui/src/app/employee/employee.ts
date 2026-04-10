import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
}

@Component({
  selector: 'app-employee',
  imports: [FormsModule, CommonModule],
  templateUrl: './employee.html',
  styleUrl: './employee.css'
})
export class EmployeeComponent implements OnInit {

  apiUrl = 'http://localhost:8080/api/employees';
  employees: Employee[] = [];
  newEmployee: Employee = { firstName: '', lastName: '', email: '' };
  loading = false;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees() {
    this.loading = true;
    this.error = null;
    this.http.get<Employee[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.employees = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading employees:', err);
        this.error = 'Failed to load employees. Backend may be down.';
        this.loading = false;
      }
    });
  }

  addEmployee() {
    if (!this.newEmployee.firstName || !this.newEmployee.lastName || !this.newEmployee.email) {
      this.error = 'Please fill in all fields';
      return;
    }
    
    this.loading = true;
    this.error = null;
    this.http.post<Employee>(this.apiUrl, this.newEmployee).subscribe({
      next: (emp) => {
        this.employees.push(emp);
        this.newEmployee = { firstName: '', lastName: '', email: '' };
        this.loading = false;
        alert('Employee added successfully!');
      },
      error: (err) => {
        console.error('Error adding employee:', err);
        this.error = 'Failed to add employee. Backend may be down.';
        this.loading = false;
      }
    });
  }
}

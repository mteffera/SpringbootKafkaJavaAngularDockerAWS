import { Component, signal } from '@angular/core';
import { EmployeeComponent } from './employee/employee';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  imports: [EmployeeComponent, HttpClientModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('employee-ui');
}

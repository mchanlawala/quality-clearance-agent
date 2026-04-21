import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { QualityService } from '../../services/quality.service';
import { ReworkOrder, ReworkOperation } from '../../models/quality.model';

@Component({
  selector: 'app-rework',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTableModule, MatSnackBarModule],
  templateUrl: './rework.component.html',
  styleUrls: ['./rework.component.css']
})
export class ReworkComponent implements OnInit {
  reworkOrders: ReworkOrder[] = [];
  newRework: ReworkOrder = {
    originalProductionOrderId: '',
    affectedBatch: '',
    plannedQuantity: 1,
    operations: [{ operationId: 0, workCenter: '', description: '', setupHours: 0, processingHoursPerPanel: 0 }]
  };
  isLoading = false;
  displayedColumns = ['reworkOrderId', 'originalOrderId', 'batch', 'quantity', 'status', 'createdAt'];

  constructor(private qualityService: QualityService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadReworkOrders();
  }

  loadReworkOrders() {
    this.qualityService.getReworkOrders().subscribe({
      next: (data) => { this.reworkOrders = data; },
      error: () => {}
    });
  }

  addOperation() {
    this.newRework.operations.push({ operationId: 0, workCenter: '', description: '', setupHours: 0, processingHoursPerPanel: 0 });
  }

  removeOperation(index: number) {
    this.newRework.operations.splice(index, 1);
  }

  submitRework() {
    if (!this.newRework.originalProductionOrderId || !this.newRework.affectedBatch) {
      this.snackBar.open('Order ID and Batch are required', 'Close');
      return;
    }
    this.isLoading = true;
    this.qualityService.createRework(this.newRework).subscribe({
      next: (res) => {
        this.snackBar.open(`Rework order ${res.reworkOrderId} created`, 'Close', { duration: 3000 });
        this.loadReworkOrders();
        this.resetForm();
        this.isLoading = false;
      },
      error: (err) => { this.snackBar.open(`Error: ${err.message}`, 'Close'); this.isLoading = false; }
    });
  }

  resetForm() {
    this.newRework = {
      originalProductionOrderId: '',
      affectedBatch: '',
      plannedQuantity: 1,
      operations: [{ operationId: 0, workCenter: '', description: '', setupHours: 0, processingHoursPerPanel: 0 }]
    };
  }
}
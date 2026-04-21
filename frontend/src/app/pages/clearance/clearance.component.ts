import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { QualityService } from '../../services/quality.service';
import { QualityClearanceStatus, AcknowledgmentRequest } from '../../models/quality.model';

@Component({
  selector: 'app-clearance',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatTableModule, MatSelectModule, MatFormFieldModule, MatInputModule, MatSnackBarModule],
  templateUrl: './clearance.component.html',
  styleUrls: ['./clearance.component.css']
})
export class ClearanceComponent implements OnInit {
  isLoading = true;
  pendingClearances: QualityClearanceStatus[] = [];
  selectedClearance: QualityClearanceStatus | null = null;
  judgment = '';
  remarks = '';
  displayedColumns = ['productionOrderId', 'batchNumber', 'operationId', 'inspectionId', 'status', 'judgment', 'actions'];

  constructor(private qualityService: QualityService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadPending();
  }

  loadPending() {
    this.isLoading = true;
    this.qualityService.getPendingClearances().subscribe({
      next: (data) => { this.pendingClearances = data; this.isLoading = false; },
      error: (err) => { this.snackBar.open(`Error: ${err.message}`, 'Close'); this.isLoading = false; }
    });
  }

  openAcknowledge(clearance: QualityClearanceStatus) {
    this.selectedClearance = clearance;
    this.judgment = clearance.judgment || 'PASS';
    this.remarks = '';
  }

  submitAcknowledge() {
    if (!this.selectedClearance) return;
    const request: AcknowledgmentRequest = {
      productionOrderId: this.selectedClearance.productionOrderId,
      batchNumber: this.selectedClearance.batchNumber,
      operationId: this.selectedClearance.operationId,
      inspectionId: this.selectedClearance.inspectionId,
      judgment: this.judgment,
      remarks: this.remarks
    };
    this.qualityService.acknowledge(request).subscribe({
      next: () => {
        this.snackBar.open('Clearance acknowledged', 'Close', { duration: 3000 });
        this.selectedClearance = null;
        this.loadPending();
      },
      error: (err) => this.snackBar.open(`Error: ${err.message}`, 'Close')
    });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      CLEARED: 'chip-green', DEFECT: 'chip-red', REWORK_REQUIRED: 'chip-orange', PENDING: 'chip-blue', MISSING: 'chip-grey'
    };
    return map[status] || 'chip-grey';
  }
}
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { QualityService } from '../../services/quality.service';
import { DefectReport, QualityClearanceStatus } from '../../models/quality.model';

@Component({
  selector: 'app-defect-entry',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSnackBarModule],
  templateUrl: './defect-entry.component.html',
  styleUrls: ['./defect-entry.component.css']
})
export class DefectEntryComponent implements OnInit {
  defect: DefectReport = {
    productionOrderId: '',
    batchNumber: '',
    operationId: 0,
    inspectionId: '',
    defectCode: '',
    description: '',
    quantityAffected: 1,
    severity: 'Major',
    reworkRequired: false,
    remarks: ''
  };
  statuses: QualityClearanceStatus[] = [];
  isLoading = false;

  constructor(private qualityService: QualityService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadStatuses();
  }

  loadStatuses() {
    this.qualityService.getAllStatuses().subscribe({
      next: (data) => { this.statuses = data.filter(s => s.status === 'DEFECT' || s.status === 'PENDING'); },
      error: () => {}
    });
  }

  submitDefect() {
    if (!this.defect.productionOrderId || !this.defect.batchNumber) {
      this.snackBar.open('Please select order and batch', 'Close');
      return;
    }
    this.isLoading = true;
    this.qualityService.createDefect(this.defect).subscribe({
      next: (res) => {
        this.snackBar.open(`Defect ${res.defectId} created`, 'Close', { duration: 3000 });
        this.resetForm();
        this.isLoading = false;
      },
      error: (err) => { this.snackBar.open(`Error: ${err.message}`, 'Close'); this.isLoading = false; }
    });
  }

  resetForm() {
    this.defect = {
      productionOrderId: '',
      batchNumber: '',
      operationId: 0,
      inspectionId: '',
      defectCode: '',
      description: '',
      quantityAffected: 1,
      severity: 'Major',
      reworkRequired: false,
      remarks: ''
    };
  }
}
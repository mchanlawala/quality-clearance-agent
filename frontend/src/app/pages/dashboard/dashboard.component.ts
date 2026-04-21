// src/app/pages/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { QualityService } from '../../services/quality.service';
import { QualityClearanceStatus, DefectReport, ReworkOrder } from '../../models/quality.model';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  isLoading = true;
  errorMsg = '';
  statuses: QualityClearanceStatus[] = [];
  defects: DefectReport[] = [];
  reworkOrders: ReworkOrder[] = [];
  insights = '';
  insightsLoading = false;

  statusChartData: ChartData<'pie'> = { labels: [], datasets: [] };
  chartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } }
  };

  constructor(private qualityService: QualityService) {}

  ngOnInit() {
    // Trigger backend evaluation first, then load dashboard
    this.qualityService.evaluateAll().subscribe({
      next: () => this.loadDashboard(),
      error: (err) => {
        console.error('Evaluation failed', err);
        this.loadDashboard(); // still try to load whatever data exists
      }
    });
  }

  // Getters remain unchanged
  get clearedCount(): number {
    return this.statuses.filter(s => s.status === 'CLEARED').length;
  }

  get defectStatusCount(): number {
    return this.statuses.filter(s => s.status === 'DEFECT').length;
  }

  get reworkRequiredCount(): number {
    return this.statuses.filter(s => s.status === 'REWORK_REQUIRED').length;
  }

  get pendingCount(): number {
    return this.statuses.filter(s => s.status === 'PENDING' || s.status === 'MISSING').length;
  }

  loadDashboard() {
    this.isLoading = true;
    this.qualityService.getDashboard().subscribe({
      next: (data) => {
        this.statuses = data.clearanceStatus || [];
        this.defects = data.defects || [];
        this.reworkOrders = data.reworkOrders || [];
        this.buildStatusChart();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMsg = err.message;
        this.isLoading = false;
      }
    });
  }

  loadInsights() {
    this.insightsLoading = true;
    this.qualityService.getInsights().subscribe({
      next: (res) => { this.insights = res.insights; this.insightsLoading = false; },
      error: () => { this.insights = 'Failed to load insights.'; this.insightsLoading = false; }
    });
  }

  buildStatusChart() {
    const cleared = this.statuses.filter(s => s.status === 'CLEARED').length;
    const defects = this.statuses.filter(s => s.status === 'DEFECT').length;
    const rework = this.statuses.filter(s => s.status === 'REWORK_REQUIRED').length;
    const pending = this.statuses.filter(s => s.status === 'PENDING' || s.status === 'MISSING').length;
    this.statusChartData = {
      labels: ['Cleared', 'Defect', 'Rework Required', 'Pending'],
      datasets: [{ data: [cleared, defects, rework, pending], backgroundColor: ['#66BB6A', '#EF5350', '#FFA726', '#42A5F5'] }]
    };
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      CLEARED: 'chip-green',
      DEFECT: 'chip-red',
      REWORK_REQUIRED: 'chip-orange',
      PENDING: 'chip-blue',
      MISSING: 'chip-grey'
    };
    return map[status] || 'chip-grey';
  }
}
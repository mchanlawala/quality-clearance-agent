import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { QualityClearanceStatus, DefectReport, ReworkOrder, AcknowledgmentRequest, DashboardSummary } from '../models/quality.model';

@Injectable({ providedIn: 'root' })
export class QualityService {
  private api = '/api/quality';

  constructor(private http: HttpClient) {}

  // Trigger evaluation
  evaluateAll(): Observable<any> {
    return this.http.post(`${this.api}/evaluate`, {}).pipe(catchError(this.handleError));
  }

  // Get pending clearances
  getPendingClearances(): Observable<QualityClearanceStatus[]> {
    return this.http.get<QualityClearanceStatus[]>(`${this.api}/pending`).pipe(catchError(this.handleError));
  }

  // Get all statuses
  getAllStatuses(): Observable<QualityClearanceStatus[]> {
    return this.http.get<QualityClearanceStatus[]>(`${this.api}/status`).pipe(catchError(this.handleError));
  }

  // Acknowledge clearance
  acknowledge(request: AcknowledgmentRequest): Observable<QualityClearanceStatus> {
    return this.http.post<QualityClearanceStatus>(`${this.api}/acknowledge`, request).pipe(catchError(this.handleError));
  }

  // Create defect report
  createDefect(report: DefectReport): Observable<DefectReport> {
    return this.http.post<DefectReport>(`${this.api}/defect`, report).pipe(catchError(this.handleError));
  }

  // Create rework order
  createRework(rework: ReworkOrder): Observable<ReworkOrder> {
    return this.http.post<ReworkOrder>(`${this.api}/rework`, rework).pipe(catchError(this.handleError));
  }

  // Get all defect reports
  getDefects(): Observable<DefectReport[]> {
    return this.http.get<DefectReport[]>(`${this.api}/defects`).pipe(catchError(this.handleError));
  }

  // Get all rework orders
  getReworkOrders(): Observable<ReworkOrder[]> {
    return this.http.get<ReworkOrder[]>(`${this.api}/rework`).pipe(catchError(this.handleError));
  }

  // Get AI insights
  getInsights(): Observable<{ insights: string }> {
    return this.http.get<{ insights: string }>(`${this.api}/insights`).pipe(catchError(this.handleError));
  }

  // Get dashboard data
  getDashboard(): Observable<any> {
    return this.http.get<any>(`${this.api}/dashboard`).pipe(catchError(this.handleError));
  }

  private handleError(err: HttpErrorResponse) {
    const msg = err.error?.error || err.message || 'Unknown error';
    return throwError(() => new Error(msg));
  }
}
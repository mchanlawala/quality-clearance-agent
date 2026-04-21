export interface Inspection {
  inspectionId: string;
  operationId: number;
  type: string;
  description: string;
  required: boolean;
  specification: any;
  status: string;
  scheduledTime: string;
  measuredValue: any;
  judgment: string;
  remarks: string;
}

export interface ProductionOrder {
  productionOrderId: string;
  salesOrderId: string;
  material: string;
  plannedQuantity: number;
  status: string;
  inspections: Inspection[];
}

export interface QualityClearanceStatus {
  productionOrderId: string;
  batchNumber: string;
  operationId: number;
  inspectionId: string;
  status: string;       // PENDING, CLEARED, DEFECT, REWORK_REQUIRED, MISSING
  judgment: string;
  acknowledgedBy: string;
  acknowledgedAt: string;
  remarks: string;
  defectId: string;
  reworkOrderId: string;
}

export interface DefectReport {
  defectId?: string;
  productionOrderId: string;
  batchNumber: string;
  operationId: number;
  inspectionId: string;
  defectCode: string;
  description: string;
  quantityAffected: number;
  severity: string;     // Minor, Major, Critical
  reworkRequired: boolean;
  createdBy?: string;
  createdAt?: string;
  remarks?: string;
}

export interface ReworkOperation {
  operationId: number;
  workCenter: string;
  description: string;
  setupHours: number;
  processingHoursPerPanel: number;
}

export interface ReworkOrder {
  reworkOrderId?: string;
  originalProductionOrderId: string;
  affectedBatch: string;
  plannedQuantity: number;
  operations: ReworkOperation[];
  status?: string;
  createdAt?: string;
}

export interface AcknowledgmentRequest {
  productionOrderId: string;
  batchNumber: string;
  operationId: number;
  inspectionId: string;
  judgment: string;
  remarks: string;
}

export interface DashboardSummary {
  totalInspections: number;
  cleared: number;
  defects: number;
  reworkRequired: number;
}
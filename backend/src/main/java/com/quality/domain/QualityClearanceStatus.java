package com.quality.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QualityClearanceStatus {
    private String productionOrderId;
    private String batchNumber;
    private Integer operationId;
    private String inspectionId;
    private String status;            // PENDING, CLEARED, DEFECT, REWORK_REQUIRED, MISSING
    private String judgment;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String remarks;
    private String defectId;
    private String reworkOrderId;
}
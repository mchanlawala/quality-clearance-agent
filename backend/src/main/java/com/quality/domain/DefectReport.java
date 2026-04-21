package com.quality.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DefectReport {
    private String defectId;
    private String productionOrderId;
    private String batchNumber;
    private Integer operationId;
    private String inspectionId;
    private String defectCode;
    private String description;
    private Integer quantityAffected;
    private String severity;          // Minor, Major, Critical
    private Boolean reworkRequired;
    private String createdBy;
    private LocalDateTime createdAt;
    private String remarks;
}
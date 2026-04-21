package com.quality.domain;

import lombok.Data;

@Data
public class AcknowledgmentRequest {
    private String productionOrderId;
    private String batchNumber;
    private Integer operationId;
    private String inspectionId;
    private String judgment;
    private String remarks;
}
package com.quality.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdoRecord {
    private String productionOrderId;
    private Integer operationId;
    private String batchNumber;
    private Integer goodQuantity;
    private Integer scrapQuantity;
    private String scrapReasonCode;
    private Double qualityPassRate;
    private Object measuredValue;
    private String judgment;
    private LocalDateTime createdAt;
}
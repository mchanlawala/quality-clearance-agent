package com.quality.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdiRecord {
    private String productionOrderId;
    private Integer operationId;
    private String batchNumber;
    private LocalDateTime actualStart;
    private LocalDateTime actualFinish;
    private Integer processedQuantity;
    private Long downtimeSeconds;
    private Boolean autoCreated;
    private String ruleId;
    private LocalDateTime recordedAt;
}
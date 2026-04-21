package com.quality.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReworkOrder {
    private String reworkOrderId;
    private String originalProductionOrderId;
    private String affectedBatch;
    private Integer plannedQuantity;
    private List<ReworkOperation> operations;
    private String status;
    private LocalDateTime createdAt;

    @Data
    public static class ReworkOperation {
        private Integer operationId;
        private String workCenter;
        private String description;
        private Double setupHours;
        private Double processingHoursPerPanel;
    }
}
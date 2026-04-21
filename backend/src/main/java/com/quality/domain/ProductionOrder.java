package com.quality.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductionOrder {
    private String productionOrderId;
    private String salesOrderId;
    private String material;
    private Integer plannedQuantity;
    private LocalDateTime basicStartDate;
    private LocalDateTime basicFinishDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualFinishDate;
    private String status;
    private List<Inspection> inspections;
    private Long totalLeadTimeMinutes;
    private String delayReason;
}
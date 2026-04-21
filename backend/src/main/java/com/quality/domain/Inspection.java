package com.quality.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Inspection {
    private String inspectionId;
    private Integer operationId;
    private String type;
    private String description;
    private Boolean required;
    private Map<String, Object> specification;
    private Object result;
    private String status;
    private LocalDateTime scheduledTime;
    private LocalDateTime completedTime;
    private Object measuredValue;
    private String judgment;
    private String remarks;
}
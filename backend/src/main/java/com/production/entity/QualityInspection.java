package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityInspection {
    private String id;
    private String workOrderId;
    private Integer inspectedQuantity;
    private Integer passedQuantity;
    private Integer failedQuantity;
    private String inspector;
    private String inspectionResult;
    private String failureReason;
    private LocalDateTime inspectionTime;
    private LocalDateTime createdAt;
}

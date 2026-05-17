package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderPauseRecord {
    private String id;
    private String workOrderId;
    private String pauseReason;
    private LocalDateTime pauseTime;
    private LocalDateTime resumeTime;
    private Boolean isResumed;
}

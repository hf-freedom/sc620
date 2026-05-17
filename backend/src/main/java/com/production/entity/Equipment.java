package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class Equipment {
    private String id;
    private String equipmentNo;
    private String equipmentName;
    private Integer capacityPerHour;
    private String status;
    private String currentWorkOrderId;
    private Map<String, Boolean> shiftAvailability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

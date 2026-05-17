package com.production.entity;

import com.production.enums.WorkOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class WorkOrder {
    private String id;
    private String workOrderNo;
    private String salesOrderId;
    private String productName;
    private Integer quantity;
    private Integer completedQuantity;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime deliveryDate;
    private WorkOrderStatus status;
    private String equipmentId;
    private String shiftId;
    private String pauseReason;
    private String parentWorkOrderId;
    private Boolean isRework;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Integer> requiredMaterials;
    private List<String> qualityInspectionIds;
}

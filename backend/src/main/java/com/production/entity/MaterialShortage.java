package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialShortage {
    private String id;
    private String workOrderId;
    private String materialCode;
    private String materialName;
    private Integer requiredQuantity;
    private Integer availableQuantity;
    private Integer shortageQuantity;
    private LocalDateTime createdAt;
    private Boolean resolved;
}

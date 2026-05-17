package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialInventory {
    private String id;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String unit;
    private LocalDateTime updatedAt;
}

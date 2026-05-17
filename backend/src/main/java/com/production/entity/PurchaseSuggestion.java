package com.production.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PurchaseSuggestion {
    private String id;
    private String materialCode;
    private String materialName;
    private Integer requiredQuantity;
    private String suggestedSupplier;
    private LocalDateTime suggestedDeliveryDate;
    private LocalDateTime createdAt;
    private Boolean processed;
}

package com.production.entity;

import com.production.enums.SalesOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesOrder {
    private String id;
    private String orderNo;
    private String customerName;
    private String productName;
    private Integer quantity;
    private LocalDateTime deliveryDate;
    private SalesOrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> workOrderIds;
}

package com.production.service;

import com.production.entity.SalesOrder;
import com.production.entity.WorkOrder;
import com.production.enums.SalesOrderStatus;
import com.production.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SalesOrderService {

    @Autowired
    private WorkOrderService workOrderService;

    private static int orderCounter = 1;

    public List<SalesOrder> getAllSalesOrders() {
        return new ArrayList<>(DataStorage.salesOrders.values());
    }

    public SalesOrder getSalesOrderById(String id) {
        return DataStorage.salesOrders.get(id);
    }

    public SalesOrder createSalesOrder(SalesOrder order) {
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo("SO" + String.format("%06d", orderCounter++));
        order.setStatus(SalesOrderStatus.DRAFT);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setWorkOrderIds(new ArrayList<>());
        DataStorage.salesOrders.put(order.getId(), order);
        return order;
    }

    public SalesOrder confirmSalesOrder(String id) {
        SalesOrder order = DataStorage.salesOrders.get(id);
        if (order == null || order.getStatus() != SalesOrderStatus.DRAFT) {
            return null;
        }

        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());

        List<WorkOrder> workOrders = workOrderService.createWorkOrdersFromSalesOrder(order);
        for (WorkOrder wo : workOrders) {
            order.getWorkOrderIds().add(wo.getId());
            workOrderService.checkMaterialAvailability(wo);
        }

        return order;
    }
}

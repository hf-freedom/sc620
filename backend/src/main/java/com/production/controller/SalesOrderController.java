package com.production.controller;

import com.production.entity.SalesOrder;
import com.production.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @GetMapping
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderService.getAllSalesOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getSalesOrderById(@PathVariable String id) {
        SalesOrder order = salesOrderService.getSalesOrderById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public SalesOrder createSalesOrder(@RequestBody SalesOrder order) {
        return salesOrderService.createSalesOrder(order);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SalesOrder> confirmSalesOrder(@PathVariable String id) {
        SalesOrder order = salesOrderService.confirmSalesOrder(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.badRequest().build();
    }
}

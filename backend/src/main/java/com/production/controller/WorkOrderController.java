package com.production.controller;

import com.production.entity.*;
import com.production.service.WorkOrderService;
import com.production.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @GetMapping
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderService.getAllWorkOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable String id) {
        WorkOrder workOrder = workOrderService.getWorkOrderById(id);
        return workOrder != null ? ResponseEntity.ok(workOrder) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<String> scheduleWorkOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String equipmentId = request.get("equipmentId");
        String shiftId = request.get("shiftId");
        LocalDateTime startTime = LocalDateTime.parse(request.get("startTime"));

        boolean success = workOrderService.scheduleWorkOrder(id, equipmentId, shiftId, startTime);
        return success ? ResponseEntity.ok("排产成功") : ResponseEntity.badRequest().body("排产失败");
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<String> startProduction(@PathVariable String id) {
        boolean success = workOrderService.startProduction(id);
        return success ? ResponseEntity.ok("开始生产") : ResponseEntity.badRequest().body("开始生产失败");
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<String> pauseWorkOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        boolean success = workOrderService.pauseWorkOrder(id, reason);
        return success ? ResponseEntity.ok("暂停成功") : ResponseEntity.badRequest().body("暂停失败");
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeWorkOrder(@PathVariable String id) {
        boolean success = workOrderService.completeWorkOrder(id);
        return success ? ResponseEntity.ok("完成生产") : ResponseEntity.badRequest().body("完成生产失败");
    }

    @GetMapping("/materials")
    public List<MaterialInventory> getAllMaterials() {
        return new ArrayList<>(DataStorage.materialInventories.values());
    }

    @GetMapping("/equipments")
    public List<Equipment> getAllEquipments() {
        return new ArrayList<>(DataStorage.equipments.values());
    }

    @GetMapping("/shifts")
    public List<Shift> getAllShifts() {
        return new ArrayList<>(DataStorage.shifts.values());
    }

    @GetMapping("/material-shortages")
    public List<MaterialShortage> getAllMaterialShortages() {
        return new ArrayList<>(DataStorage.materialShortages.values());
    }

    @GetMapping("/purchase-suggestions")
    public List<PurchaseSuggestion> getAllPurchaseSuggestions() {
        return new ArrayList<>(DataStorage.purchaseSuggestions.values());
    }

    @GetMapping("/quality-inspections")
    public List<QualityInspection> getAllQualityInspections() {
        return new ArrayList<>(DataStorage.qualityInspections.values());
    }

    @PostMapping("/quality-inspections/{id}/process")
    public ResponseEntity<String> processQualityInspection(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        int passedQty = (int) request.get("passedQuantity");
        int failedQty = (int) request.get("failedQuantity");
        String inspector = (String) request.get("inspector");
        String failureReason = (String) request.get("failureReason");

        boolean success = workOrderService.processQualityInspection(id, passedQty, failedQty, inspector, failureReason);
        return success ? ResponseEntity.ok("质检完成") : ResponseEntity.badRequest().body("质检处理失败");
    }
}

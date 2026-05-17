package com.production.service;

import com.production.entity.*;
import com.production.enums.WorkOrderStatus;
import com.production.storage.DataStorage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private static int workOrderCounter = 1;

    public List<WorkOrder> getAllWorkOrders() {
        return new ArrayList<>(DataStorage.workOrders.values());
    }

    public WorkOrder getWorkOrderById(String id) {
        return DataStorage.workOrders.get(id);
    }

    public List<WorkOrder> createWorkOrdersFromSalesOrder(SalesOrder salesOrder) {
        List<WorkOrder> workOrders = new ArrayList<>();
        int totalQuantity = salesOrder.getQuantity();
        int batchSize = 100;
        int batchCount = (totalQuantity + batchSize - 1) / batchSize;

        Map<String, Integer> requiredMaterials = new HashMap<>();
        requiredMaterials.put("M001", 10);
        requiredMaterials.put("M002", 5);
        requiredMaterials.put("M003", 2);
        requiredMaterials.put("M004", 20);
        requiredMaterials.put("M005", 1);

        for (int i = 0; i < batchCount; i++) {
            int qty = (i == batchCount - 1) ? (totalQuantity - i * batchSize) : batchSize;

            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(UUID.randomUUID().toString());
            workOrder.setWorkOrderNo("WO" + String.format("%06d", workOrderCounter++));
            workOrder.setSalesOrderId(salesOrder.getId());
            workOrder.setProductName(salesOrder.getProductName());
            workOrder.setQuantity(qty);
            workOrder.setCompletedQuantity(0);
            workOrder.setDeliveryDate(salesOrder.getDeliveryDate());
            workOrder.setStatus(WorkOrderStatus.PENDING);
            workOrder.setIsRework(false);
            workOrder.setCreatedAt(LocalDateTime.now());
            workOrder.setUpdatedAt(LocalDateTime.now());
            workOrder.setRequiredMaterials(new HashMap<>(requiredMaterials));
            workOrder.setQualityInspectionIds(new ArrayList<>());

            DataStorage.workOrders.put(workOrder.getId(), workOrder);
            workOrders.add(workOrder);
        }

        return workOrders;
    }

    public boolean checkMaterialAvailability(WorkOrder workOrder) {
        Map<String, Integer> requiredMaterials = workOrder.getRequiredMaterials();
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String materialCode = entry.getKey();
            int requiredQty = entry.getValue() * workOrder.getQuantity();

            Optional<MaterialInventory> inventoryOpt = DataStorage.materialInventories.values()
                    .stream()
                    .filter(m -> m.getMaterialCode().equals(materialCode))
                    .findFirst();

            if (!inventoryOpt.isPresent()) {
                createMaterialShortage(workOrder, materialCode, requiredQty, 0);
                return false;
            }

            MaterialInventory inventory = inventoryOpt.get();
            if (inventory.getAvailableQuantity() < requiredQty) {
                createMaterialShortage(workOrder, materialCode, requiredQty, inventory.getAvailableQuantity());
                return false;
            }
        }

        workOrder.setStatus(WorkOrderStatus.READY);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return true;
    }

    private void createMaterialShortage(WorkOrder workOrder, String materialCode, int requiredQty, int availableQty) {
        MaterialShortage shortage = new MaterialShortage();
        shortage.setId(UUID.randomUUID().toString());
        shortage.setWorkOrderId(workOrder.getId());
        shortage.setMaterialCode(materialCode);
        shortage.setMaterialName(DataStorage.materialInventories.values().stream()
                .filter(m -> m.getMaterialCode().equals(materialCode))
                .findFirst()
                .map(MaterialInventory::getMaterialName)
                .orElse("未知物料"));
        shortage.setRequiredQuantity(requiredQty);
        shortage.setAvailableQuantity(availableQty);
        shortage.setShortageQuantity(requiredQty - availableQty);
        shortage.setCreatedAt(LocalDateTime.now());
        shortage.setResolved(false);
        DataStorage.materialShortages.put(shortage.getId(), shortage);

        createPurchaseSuggestion(shortage);
    }

    private void createPurchaseSuggestion(MaterialShortage shortage) {
        PurchaseSuggestion suggestion = new PurchaseSuggestion();
        suggestion.setId(UUID.randomUUID().toString());
        suggestion.setMaterialCode(shortage.getMaterialCode());
        suggestion.setMaterialName(shortage.getMaterialName());
        suggestion.setRequiredQuantity(shortage.getShortageQuantity());
        suggestion.setSuggestedSupplier("默认供应商");
        suggestion.setSuggestedDeliveryDate(LocalDateTime.now().plusDays(7));
        suggestion.setCreatedAt(LocalDateTime.now());
        suggestion.setProcessed(false);
        DataStorage.purchaseSuggestions.put(suggestion.getId(), suggestion);
    }

    public boolean scheduleWorkOrder(String workOrderId, String equipmentId, String shiftId, LocalDateTime startTime) {
        WorkOrder workOrder = DataStorage.workOrders.get(workOrderId);
        if (workOrder == null || workOrder.getStatus() != WorkOrderStatus.READY) {
            return false;
        }

        Equipment equipment = DataStorage.equipments.get(equipmentId);
        if (equipment == null || !"AVAILABLE".equals(equipment.getStatus())) {
            return false;
        }

        Shift shift = DataStorage.shifts.values().stream()
                .filter(s -> s.getId().equals(shiftId))
                .findFirst()
                .orElse(null);
        if (shift == null) {
            return false;
        }

        int hoursNeeded = (workOrder.getQuantity() + equipment.getCapacityPerHour() - 1) / equipment.getCapacityPerHour();
        LocalDateTime endTime = startTime.plusHours(hoursNeeded);

        workOrder.setEquipmentId(equipmentId);
        workOrder.setShiftId(shiftId);
        workOrder.setScheduledStartTime(startTime);
        workOrder.setScheduledEndTime(endTime);
        workOrder.setStatus(WorkOrderStatus.SCHEDULED);
        workOrder.setUpdatedAt(LocalDateTime.now());

        return true;
    }

    public boolean startProduction(String workOrderId) {
        WorkOrder workOrder = DataStorage.workOrders.get(workOrderId);
        if (workOrder == null || workOrder.getStatus() != WorkOrderStatus.SCHEDULED) {
            return false;
        }

        reserveMaterials(workOrder);

        Equipment equipment = DataStorage.equipments.get(workOrder.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("IN_USE");
            equipment.setCurrentWorkOrderId(workOrderId);
            equipment.setUpdatedAt(LocalDateTime.now());
        }

        workOrder.setStatus(WorkOrderStatus.IN_PRODUCTION);
        workOrder.setActualStartTime(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        return true;
    }

    private void reserveMaterials(WorkOrder workOrder) {
        Map<String, Integer> requiredMaterials = workOrder.getRequiredMaterials();
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String materialCode = entry.getKey();
            int requiredQty = entry.getValue() * workOrder.getQuantity();

            DataStorage.materialInventories.values().stream()
                    .filter(m -> m.getMaterialCode().equals(materialCode))
                    .findFirst()
                    .ifPresent(inventory -> {
                        inventory.setReservedQuantity(inventory.getReservedQuantity() + requiredQty);
                        inventory.setAvailableQuantity(inventory.getQuantity() - inventory.getReservedQuantity());
                        inventory.setUpdatedAt(LocalDateTime.now());
                    });
        }
    }

    public boolean pauseWorkOrder(String workOrderId, String reason) {
        WorkOrder workOrder = DataStorage.workOrders.get(workOrderId);
        if (workOrder == null || workOrder.getStatus() != WorkOrderStatus.IN_PRODUCTION) {
            return false;
        }

        releaseMaterialsPartially(workOrder);

        Equipment equipment = DataStorage.equipments.get(workOrder.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("AVAILABLE");
            equipment.setCurrentWorkOrderId(null);
            equipment.setUpdatedAt(LocalDateTime.now());
        }

        WorkOrderPauseRecord pauseRecord = new WorkOrderPauseRecord();
        pauseRecord.setId(UUID.randomUUID().toString());
        pauseRecord.setWorkOrderId(workOrderId);
        pauseRecord.setPauseReason(reason);
        pauseRecord.setPauseTime(LocalDateTime.now());
        pauseRecord.setIsResumed(false);
        DataStorage.pauseRecords.put(pauseRecord.getId(), pauseRecord);

        workOrder.setStatus(WorkOrderStatus.PAUSED);
        workOrder.setPauseReason(reason);
        workOrder.setUpdatedAt(LocalDateTime.now());

        return true;
    }

    private void releaseMaterialsPartially(WorkOrder workOrder) {
        Map<String, Integer> requiredMaterials = workOrder.getRequiredMaterials();
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String materialCode = entry.getKey();
            int reservedQty = entry.getValue() * workOrder.getQuantity();
            int releasedQty = reservedQty / 2;

            DataStorage.materialInventories.values().stream()
                    .filter(m -> m.getMaterialCode().equals(materialCode))
                    .findFirst()
                    .ifPresent(inventory -> {
                        inventory.setReservedQuantity(inventory.getReservedQuantity() - releasedQty);
                        inventory.setAvailableQuantity(inventory.getQuantity() - inventory.getReservedQuantity());
                        inventory.setUpdatedAt(LocalDateTime.now());
                    });
        }
    }

    public boolean completeWorkOrder(String workOrderId) {
        WorkOrder workOrder = DataStorage.workOrders.get(workOrderId);
        if (workOrder == null || workOrder.getStatus() != WorkOrderStatus.IN_PRODUCTION) {
            return false;
        }

        consumeMaterials(workOrder);

        Equipment equipment = DataStorage.equipments.get(workOrder.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("AVAILABLE");
            equipment.setCurrentWorkOrderId(null);
            equipment.setUpdatedAt(LocalDateTime.now());
        }

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setActualEndTime(LocalDateTime.now());
        workOrder.setCompletedQuantity(workOrder.getQuantity());
        workOrder.setUpdatedAt(LocalDateTime.now());

        createQualityInspection(workOrder);

        return true;
    }

    private void consumeMaterials(WorkOrder workOrder) {
        Map<String, Integer> requiredMaterials = workOrder.getRequiredMaterials();
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String materialCode = entry.getKey();
            int consumedQty = entry.getValue() * workOrder.getQuantity();

            DataStorage.materialInventories.values().stream()
                    .filter(m -> m.getMaterialCode().equals(materialCode))
                    .findFirst()
                    .ifPresent(inventory -> {
                        inventory.setQuantity(inventory.getQuantity() - consumedQty);
                        inventory.setReservedQuantity(inventory.getReservedQuantity() - consumedQty);
                        inventory.setAvailableQuantity(inventory.getQuantity() - inventory.getReservedQuantity());
                        inventory.setUpdatedAt(LocalDateTime.now());
                    });
        }
    }

    private void createQualityInspection(WorkOrder workOrder) {
        QualityInspection inspection = new QualityInspection();
        inspection.setId(UUID.randomUUID().toString());
        inspection.setWorkOrderId(workOrder.getId());
        inspection.setInspectedQuantity(workOrder.getQuantity());
        inspection.setCreatedAt(LocalDateTime.now());
        DataStorage.qualityInspections.put(inspection.getId(), inspection);

        workOrder.getQualityInspectionIds().add(inspection.getId());
    }

    public boolean processQualityInspection(String inspectionId, int passedQty, int failedQty, String inspector, String failureReason) {
        QualityInspection inspection = DataStorage.qualityInspections.get(inspectionId);
        if (inspection == null) {
            return false;
        }

        inspection.setPassedQuantity(passedQty);
        inspection.setFailedQuantity(failedQty);
        inspection.setInspector(inspector);
        inspection.setInspectionTime(LocalDateTime.now());

        WorkOrder workOrder = DataStorage.workOrders.get(inspection.getWorkOrderId());
        if (workOrder != null) {
            if (failedQty == 0) {
                inspection.setInspectionResult("PASSED");
                workOrder.setStatus(WorkOrderStatus.QUALITY_PASSED);
            } else {
                inspection.setInspectionResult("FAILED");
                inspection.setFailureReason(failureReason);
                workOrder.setStatus(WorkOrderStatus.QUALITY_FAILED);
                createReworkWorkOrder(workOrder, failedQty);
            }
            workOrder.setUpdatedAt(LocalDateTime.now());
        }

        return true;
    }

    private void createReworkWorkOrder(WorkOrder original, int reworkQty) {
        WorkOrder rework = new WorkOrder();
        rework.setId(UUID.randomUUID().toString());
        rework.setWorkOrderNo("WO" + String.format("%06d", workOrderCounter++));
        rework.setSalesOrderId(original.getSalesOrderId());
        rework.setProductName(original.getProductName() + "(返工)");
        rework.setQuantity(reworkQty);
        rework.setCompletedQuantity(0);
        rework.setDeliveryDate(original.getDeliveryDate());
        rework.setStatus(WorkOrderStatus.REWORK);
        rework.setParentWorkOrderId(original.getId());
        rework.setIsRework(true);
        rework.setCreatedAt(LocalDateTime.now());
        rework.setUpdatedAt(LocalDateTime.now());
        rework.setRequiredMaterials(new HashMap<>(original.getRequiredMaterials()));
        rework.setQualityInspectionIds(new ArrayList<>());
        DataStorage.workOrders.put(rework.getId(), rework);
    }
}

package com.production.service;

import com.production.entity.Equipment;
import com.production.entity.WorkOrder;
import com.production.enums.WorkOrderStatus;
import com.production.storage.DataStorage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    public Map<String, Object> getProductionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalOrders = DataStorage.workOrders.size();
        long completedOrders = DataStorage.workOrders.values().stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.QUALITY_PASSED)
                .count();

        double completionRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
        stats.put("completionRate", String.format("%.2f", completionRate));
        stats.put("totalWorkOrders", totalOrders);
        stats.put("completedWorkOrders", completedOrders);

        Map<String, Double> equipmentUtilization = new HashMap<>();
        for (Equipment eq : DataStorage.equipments.values()) {
            double utilization = calculateEquipmentUtilization(eq);
            equipmentUtilization.put(eq.getEquipmentName(), utilization);
        }
        stats.put("equipmentUtilization", equipmentUtilization);

        long materialShortageCount = DataStorage.materialShortages.size();
        stats.put("materialShortageCount", materialShortageCount);

        long delayedOrders = DataStorage.workOrders.values().stream()
                .filter(wo -> wo.getDeliveryDate() != null
                        && wo.getDeliveryDate().isBefore(LocalDateTime.now())
                        && wo.getStatus() != WorkOrderStatus.QUALITY_PASSED)
                .count();
        stats.put("delayedOrders", delayedOrders);

        return stats;
    }

    private double calculateEquipmentUtilization(Equipment equipment) {
        long inProductionCount = DataStorage.workOrders.values().stream()
                .filter(wo -> equipment.getId().equals(wo.getEquipmentId())
                        && wo.getActualStartTime() != null)
                .count();

        return Math.min(inProductionCount * 20.0, 100.0);
    }
}

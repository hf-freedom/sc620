package com.production.config;

import com.production.entity.*;
import com.production.storage.DataStorage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        initMaterials();
        initEquipments();
        initShifts();
    }

    private void initMaterials() {
        String[][] materials = {
                {"M001", "钢材", "1000", "吨"},
                {"M002", "塑料颗粒", "5000", "千克"},
                {"M003", "电子元件", "10000", "个"},
                {"M004", "螺丝", "50000", "个"},
                {"M005", "包装材料", "2000", "套"}
        };

        for (String[] mat : materials) {
            MaterialInventory inventory = new MaterialInventory();
            inventory.setId(UUID.randomUUID().toString());
            inventory.setMaterialCode(mat[0]);
            inventory.setMaterialName(mat[1]);
            int qty = Integer.parseInt(mat[2]);
            inventory.setQuantity(qty);
            inventory.setReservedQuantity(0);
            inventory.setAvailableQuantity(qty);
            inventory.setUnit(mat[3]);
            inventory.setUpdatedAt(LocalDateTime.now());
            DataStorage.materialInventories.put(inventory.getId(), inventory);
        }
    }

    private void initEquipments() {
        Map<String, Boolean> shiftAvailability = new HashMap<>();
        shiftAvailability.put("S001", true);
        shiftAvailability.put("S002", true);
        shiftAvailability.put("S003", true);

        String[][] equipments = {
                {"E001", "注塑机A", "50"},
                {"E002", "注塑机B", "60"},
                {"E003", "组装线1", "100"},
                {"E004", "组装线2", "80"},
                {"E005", "包装线", "200"}
        };

        for (String[] eq : equipments) {
            Equipment equipment = new Equipment();
            equipment.setId(UUID.randomUUID().toString());
            equipment.setEquipmentNo(eq[0]);
            equipment.setEquipmentName(eq[1]);
            equipment.setCapacityPerHour(Integer.parseInt(eq[2]));
            equipment.setStatus("AVAILABLE");
            equipment.setShiftAvailability(new HashMap<>(shiftAvailability));
            equipment.setCreatedAt(LocalDateTime.now());
            equipment.setUpdatedAt(LocalDateTime.now());
            DataStorage.equipments.put(equipment.getId(), equipment);
        }
    }

    private void initShifts() {
        Object[][] shifts = {
                {"S001", "早班", LocalTime.of(8, 0), LocalTime.of(16, 0), 100},
                {"S002", "中班", LocalTime.of(16, 0), LocalTime.of(0, 0), 80},
                {"S003", "晚班", LocalTime.of(0, 0), LocalTime.of(8, 0), 50}
        };

        for (Object[] s : shifts) {
            Shift shift = new Shift();
            shift.setId(UUID.randomUUID().toString());
            shift.setShiftName((String) s[1]);
            shift.setStartTime((LocalTime) s[2]);
            shift.setEndTime((LocalTime) s[3]);
            shift.setCapacity((Integer) s[4]);
            DataStorage.shifts.put(shift.getId(), shift);
        }
    }
}

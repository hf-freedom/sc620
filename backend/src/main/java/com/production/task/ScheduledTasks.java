package com.production.task;

import com.production.entity.WorkOrder;
import com.production.enums.WorkOrderStatus;
import com.production.storage.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 60000)
    public void checkDelayedOrders() {
        LocalDateTime now = LocalDateTime.now();

        for (WorkOrder workOrder : DataStorage.workOrders.values()) {
            if (workOrder.getDeliveryDate() != null
                    && workOrder.getDeliveryDate().isBefore(now)
                    && workOrder.getStatus() != WorkOrderStatus.QUALITY_PASSED
                    && workOrder.getStatus() != WorkOrderStatus.CANCELLED) {

                logger.warn("工单 {} 已延期，交付日期: {}，当前状态: {}",
                        workOrder.getWorkOrderNo(),
                        workOrder.getDeliveryDate(),
                        workOrder.getStatus());
            }
        }
    }
}

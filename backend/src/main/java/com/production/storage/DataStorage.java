package com.production.storage;

import com.production.entity.*;

import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    public static final ConcurrentHashMap<String, SalesOrder> salesOrders = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, WorkOrder> workOrders = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, MaterialInventory> materialInventories = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Equipment> equipments = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Shift> shifts = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, MaterialShortage> materialShortages = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, PurchaseSuggestion> purchaseSuggestions = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, QualityInspection> qualityInspections = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, WorkOrderPauseRecord> pauseRecords = new ConcurrentHashMap<>();
}

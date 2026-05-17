package com.production.entity;

import lombok.Data;

import java.time.LocalTime;

@Data
public class Shift {
    private String id;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
}

package com.ssafy.wtd.backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class VehicleRes {
    private Long vehicleId;
    private Long userId;

    private String model;
    private Float efficiency;
    private Float batteryCapacity;
    private Float maxRange;
    private String fastChargeType;
    private String slowChargeType;

    private LocalDateTime createdAt;
}

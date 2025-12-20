package com.ssafy.wtd.backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VehicleRes {
    private Long vehicleId;
    private Long userId;

    private String model;
    private Float efficiency;
    private Float batteryCapacity;
    private Float maxRange;
    private String dcChargeType;
    private String acChargeType;

    private LocalDateTime createdAt;
}

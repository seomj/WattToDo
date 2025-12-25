package com.ssafy.wtd.backend.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    private Long vehicleId;

    private String model;
    private Float efficiency;
    private Float batteryCapacity;
    private Float maxRange;
    private String fastChargeType; // Renamed from dcChargeType
    private String slowChargeType; // Renamed from acChargeType

    private LocalDateTime createdAt;
}
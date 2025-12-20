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
    private Long userId;

    private String model;
    private Float efficiency;
    private Float batteryCapacity;
    private Float maxRange;
    private String dcChargeType;
    private String acChargeType;

    private LocalDateTime createdAt;
}
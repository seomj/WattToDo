package com.ssafy.wtd.backend.dto.vehicle;

import lombok.Getter;

@Getter
public class VehicleUpdateReq {
    private String model;
    private Float efficiency;
    private Float batteryCapacity;
    private Float maxRange;
    private String fastChargeType;
    private String slowChargeType;
}

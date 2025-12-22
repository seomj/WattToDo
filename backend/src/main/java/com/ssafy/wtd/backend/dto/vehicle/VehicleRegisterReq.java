package com.ssafy.wtd.backend.dto.vehicle;

import lombok.Getter;

@Getter
public class VehicleRegisterReq {

    private String model;             // 필수

    private Float efficiency;         // 선택
    private Float batteryCapacity;    // 선택
    private Float maxRange;           // 선택
    private String dcChargeType;      // 선택
    private String acChargeType;      // 선택
}

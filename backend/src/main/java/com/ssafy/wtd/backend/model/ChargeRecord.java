package com.ssafy.wtd.backend.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChargeRecord {
    private Long recordId;
    private Long userId;
    private String stationId;
    private String stationName;

    private String status; // 'CHARGING', 'COMPLETED' 상태 관리

    // 충전량 및 목표
    private Float startKwh;
    private Float targetKwh;
    private Float chargedKwh;
    private Float chargerCapacity;

    // 시간 및 비용
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private Integer chargingCost;

    // 시스템 메타 데이터
    private LocalDateTime createdAt;
}
package com.ssafy.wtd.backend.dto.charge;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChargeConfirmReq { // 사용자가 확인한 최종 기록 값
    private float chargedKwh;
    private int chargingCost;
    private LocalDateTime endTime;
}
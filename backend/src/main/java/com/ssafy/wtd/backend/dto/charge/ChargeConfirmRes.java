package com.ssafy.wtd.backend.dto.charge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChargeConfirmRes { // 최종 저장 후 결과
    private boolean success;
    private Data data;
    private String message;

    @Getter
    @Setter
    public static class Data {
        private Long recordId;
        private String stationId;
        private float chargedKwh;
        private int chargingCost;
        private float durationMin;
        private float carbonSaved;
        private float totalCarbonSaved;
    }
}

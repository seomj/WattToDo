package com.ssafy.wtd.backend.dto.charge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageParsingRes { // 이미지 값으로, 사용자에게 검증 요청 시 사용
    private boolean success;
    private Data data;
    private String message;

    @Getter
    @Setter
    public static class Data {
        private Long recordId;
        private Parsed parsed;
    }

    @Getter
    @Setter
    public static class Parsed {
        private float chargedKwh;
        private int chargingCost;
        private String durationText;
    }
}

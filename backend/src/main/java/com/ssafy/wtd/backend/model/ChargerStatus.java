package com.ssafy.wtd.backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChargerStatus {

    AVAILABLE(0, "충전가능", MarkerColor.GREEN),
    CHARGING(3, "충전중", MarkerColor.BLUE),

    COMM_ERROR(1, "통신이상", MarkerColor.GRAY),
    STOPPED(4, "운영중지", MarkerColor.GRAY),
    INSPECTING(5, "점검중", MarkerColor.GRAY),
    UNKNOWN(9, "상태미확인", MarkerColor.GRAY);

    private final int code;          // 공공데이터 stat 코드
    private final String label;      // UI 표시 문자열
    private final MarkerColor markerColor;

    public static ChargerStatus from(int code) {
        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
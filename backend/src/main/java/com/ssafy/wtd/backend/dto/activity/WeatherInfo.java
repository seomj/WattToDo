package com.ssafy.wtd.backend.dto.activity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherInfo {
    private String description; // 날씨 상태 (예: 흐림, 맑음)
    private double temp;        // 기온 (°C)
    private int humidity;       // 습도 (%)
    private double windSpeed;    // 풍속 (m/s)
    private double precipitation; // 강수량 (mm) - 비나 눈이 안 오면 0.0
}

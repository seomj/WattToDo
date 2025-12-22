package com.ssafy.wtd.backend.dto.station;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 홈 지도 마커 전용 DTO
public class StationMarkerDto {

    private String stationId;
    private String stationName;

    private double lat;
    private double lng;

    private int availableCount; // 사용 가능 충전기 수
    private int totalCount;     // 전체 충전기 수

    private String markerColor; // GREEN / BLUE / GRAY

    public void setMarkerColor(String markerColor) {
        this.markerColor = markerColor;
    }
}

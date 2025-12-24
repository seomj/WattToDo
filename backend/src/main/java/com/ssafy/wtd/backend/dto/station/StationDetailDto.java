package com.ssafy.wtd.backend.dto.station;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
// 마커 클릭 시 상세 패널 전체 DTO
public class StationDetailDto {

    private String stationId;
    private String stationName;
    private String address;
    private double lat;
    private double lng;

    private List<ChargerSummaryDto> chargers;

    public StationDetailDto(
            String stationId,
            String stationName,
            String address,
            double lat,
            double lng,
            List<ChargerSummaryDto> chargers) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.chargers = chargers;
    }
}

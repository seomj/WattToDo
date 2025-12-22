package com.ssafy.wtd.backend.service.station;

import com.ssafy.wtd.backend.dto.station.ChargerSummaryDto;
import com.ssafy.wtd.backend.dto.station.StationDetailDto;
import com.ssafy.wtd.backend.repository.station.StationDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationDetailService {

    private final StationDetailRepository stationDetailRepository;

    public StationDetailDto getStationDetail(String stationId) {

        StationDetailDto station = stationDetailRepository.findStationInfo(stationId);
        if (station == null) {
            return null;
        }

        List<ChargerSummaryDto> rawChargers =
                stationDetailRepository.findChargersByStationId(stationId);

        List<ChargerSummaryDto> chargers = new ArrayList<>(rawChargers.size());
        for (ChargerSummaryDto c : rawChargers) {
            chargers.add(toViewDto(c));
        }

        return new StationDetailDto(
                station.getStationId(),
                station.getStationName(),
                station.getAddress(),
                station.getLat(),
                station.getLng(),
                chargers
        );
    }

    /**
     * DB 조회 결과(원본) → UI 표시용(라벨/색상 포함) DTO로 변환
     */
    private ChargerSummaryDto toViewDto(ChargerSummaryDto raw) {

        StatusView view = mapStatus(raw.getStatus());

        // chargeType/status는 기존 그대로 유지
        return new ChargerSummaryDto(
                raw.getChargerId(),
                raw.getChargerName(),
                raw.getChargeType(),
                raw.getStatus(),
                view.statusLabel,
                view.markerColor
        );
    }

    /**
     * status 코드 → (라벨, 색상) 매핑
     * ※ 현재 DB status 설계(0=사용가능, 3=사용중, 그 외=불가) 기준
     */
    private StatusView mapStatus(String status) {
        if ("0".equals(status)) {
            return new StatusView("사용 가능", "GREEN");
        }
        if ("3".equals(status)) {
            return new StatusView("충전 중", "BLUE");
        }
        return new StatusView("사용 불가", "GRAY");
    }

    private static class StatusView {
        private final String statusLabel;
        private final String markerColor;

        private StatusView(String statusLabel, String markerColor) {
            this.statusLabel = statusLabel;
            this.markerColor = markerColor;
        }
    }
}

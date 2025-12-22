package com.ssafy.wtd.backend.service.station;

import com.ssafy.wtd.backend.dto.station.StationMarkerDto;
import com.ssafy.wtd.backend.repository.station.StationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationQueryService {

    private final StationQueryRepository stationQueryRepository;

    /**
     * 초기 전체 조회
     */
    public List<StationMarkerDto> getAllStationMarkers() {

        List<StationMarkerDto> stations =
                stationQueryRepository.findAllStationMarkers();

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 좌표 기반 조회 (Phase 2-2)
     */
    public List<StationMarkerDto> getNearbyStationMarkers(
            double lat,
            double lng,
            int radius
    ) {

        // 대략적인 위경도 거리 환산 (미터 기준)
        double latOffset = radius / 111_000.0;
        double lngOffset = radius / (111_000.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latOffset;
        double maxLat = lat + latOffset;
        double minLng = lng - lngOffset;
        double maxLng = lng + lngOffset;

        List<StationMarkerDto> stations =
                stationQueryRepository.findNearbyStationMarkers(
                        minLat, maxLat, minLng, maxLng
                );

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 지역별 검색 (city, district)
     */
    public List<StationMarkerDto> getStationsByCityAndDistrict(
            String city,
            String district
    ) {
        List<StationMarkerDto> stations =
                stationQueryRepository.findByCityAndDistrict(city, district);

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 마커 색상 계산 (공통 로직)
     */
    private void applyMarkerColor(List<StationMarkerDto> stations) {

        for (StationMarkerDto s : stations) {

            if (s.getAvailableCount() > 0) {
                s.setMarkerColor("GREEN");
            } else if (s.getTotalCount() > 0) {
                s.setMarkerColor("BLUE");
            } else {
                s.setMarkerColor("GRAY");
            }
        }
    }
}

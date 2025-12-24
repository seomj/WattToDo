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

        List<StationMarkerDto> stations = stationQueryRepository.findAllStationMarkers();

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 좌표 기반 조회 (Phase 2-2)
     */
    public List<StationMarkerDto> getNearbyStationMarkers(
            double lat,
            double lng,
            int radius) {

        // 대략적인 위경도 거리 환산 (미터 기준)
        double latOffset = radius / 111_000.0;
        double lngOffset = radius / (111_000.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latOffset;
        double maxLat = lat + latOffset;
        double minLng = lng - lngOffset;
        double maxLng = lng + lngOffset;

        List<StationMarkerDto> stations = stationQueryRepository.findNearbyStationMarkers(
                minLat, maxLat, minLng, maxLng);

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 지역별 검색 (city, district)
     */
    public List<StationMarkerDto> getStationsByCityAndDistrict(
            String city,
            String district) {
        List<StationMarkerDto> stations = stationQueryRepository.findByCityAndDistrict(city, district);

        applyMarkerColor(stations);
        return stations;
    }

    /**
     * 키워드 검색 (이름, 주소)
     */
    public List<StationMarkerDto> getStationsByKeyword(String keyword) {
        List<StationMarkerDto> stations = stationQueryRepository.findByKeyword(keyword);
        applyMarkerColor(stations);
        return stations;
    }

    private void applyMarkerColor(List<StationMarkerDto> stations) {

        for (StationMarkerDto s : stations) {

            if (s.getTotalCount() > 0 && s.getAvailableCount() == s.getTotalCount()) {
                s.setMarkerColor("GREEN"); // 모두 이용 가능
            } else if (s.getAvailableCount() > 0) {
                s.setMarkerColor("BLUE");  // 1개 이상 이용 가능 (일부 이용 가능)
            } else {
                s.setMarkerColor("GRAY");  // 이용 불가
            }
        }
    }
}

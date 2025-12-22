package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.ApiRes;
import com.ssafy.wtd.backend.dto.station.StationMarkerDto;
import com.ssafy.wtd.backend.service.station.StationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stations")
public class StationController {

    private final StationQueryService stationQueryService;

    /**
     * 초기 전체 조회
     */
    @GetMapping
    public ApiRes<List<StationMarkerDto>> getAllStations() {
        return ApiRes.ok(
                stationQueryService.getAllStationMarkers()
        );
    }

    /**
     * 좌표 기반 조회 (Phase 2-2)
     */
    @GetMapping("/nearby")
    public ApiRes<List<StationMarkerDto>> getNearbyStations(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "2000") int radius
    ) {
        return ApiRes.ok(
                stationQueryService.getNearbyStationMarkers(lat, lng, radius)
        );
    }

    /**
     * 지역별 검색 (city, district)
     */
    @GetMapping("/search")
    public ApiRes<List<StationMarkerDto>> searchStations(
            @RequestParam String city,
            @RequestParam String district
    ) {
        return ApiRes.ok(
                stationQueryService.getStationsByCityAndDistrict(city, district)
        );
    }
}

package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.ApiRes;
import com.ssafy.wtd.backend.dto.station.StationDetailDto;
import com.ssafy.wtd.backend.service.station.StationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stations")
public class StationDetailController {

    private final StationDetailService stationDetailService;

    /**
     * 충전소 상세 조회 (마커 클릭)
     */
    @GetMapping("/{stationId}")
    public ApiRes<StationDetailDto> getStationDetail(
            @PathVariable String stationId
    ) {
        StationDetailDto detail =
                stationDetailService.getStationDetail(stationId);

        if (detail == null) {
            return ApiRes.fail("충전소 정보를 찾을 수 없습니다.");
        }

        return ApiRes.ok(detail);
    }
}

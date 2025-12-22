package com.ssafy.wtd.backend.repository.station;

import com.ssafy.wtd.backend.dto.station.StationMarkerDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StationQueryRepository {

    // 기존 전체 조회
    List<StationMarkerDto> findAllStationMarkers();

    // 좌표 기반 조회 (Phase 2-2)
    List<StationMarkerDto> findNearbyStationMarkers(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );

    // 지역별 검색 (city, district)
    List<StationMarkerDto> findByCityAndDistrict(
            @Param("city") String city,
            @Param("district") String district
    );
}

package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.Station;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationRepository {

    /**
     * station_id를 사용하여 단일 충전소 정보를 조회합니다.
     * RecordService의 위치 확인 로직에서 사용됩니다.
     * * @param stationId 조회할 충전소의 ID
     * @return Station Model 객체
     */
    Station findById(String stationId);

    // TODO: 지도 표시를 위한 목록 조회, 필터링 등 다른 메서드가 여기에 추가될 예정입니다.
}
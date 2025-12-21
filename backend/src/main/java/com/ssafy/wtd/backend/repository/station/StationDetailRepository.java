package com.ssafy.wtd.backend.repository.station;

import com.ssafy.wtd.backend.dto.station.ChargerSummaryDto;
import com.ssafy.wtd.backend.dto.station.StationDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StationDetailRepository {

    StationDetailDto findStationInfo(@Param("stationId") String stationId);

    List<ChargerSummaryDto> findChargersByStationId(@Param("stationId") String stationId);
}

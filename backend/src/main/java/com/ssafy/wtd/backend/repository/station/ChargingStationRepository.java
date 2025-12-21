package com.ssafy.wtd.backend.repository.station;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChargingStationRepository {

    void saveOrUpdate(
            @Param("stationId") String stationId,
            @Param("stationName") String stationName,
            @Param("address") String address,
            @Param("lat") double lat,
            @Param("lng") double lng
    );
}

package com.ssafy.wtd.backend.repository.station;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChargerRepository {

    void saveOrUpdate(
            @Param("chargerId") String chargerId,
            @Param("stationId") String stationId,
            @Param("status") int status,
            @Param("powerType") String powerType,
            @Param("chargerType") String chargerType);
}

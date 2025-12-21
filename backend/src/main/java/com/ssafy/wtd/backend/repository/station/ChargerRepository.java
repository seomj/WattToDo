package com.ssafy.wtd.backend.repository.station;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChargerRepository {

    void saveOrUpdate(
            @Param("chargerId") String chargerId,
            @Param("stationId") String stationId,
            @Param("chargerName") String chargerName,
            @Param("status") int status,
            @Param("chargeType") String chargeType
    );
}

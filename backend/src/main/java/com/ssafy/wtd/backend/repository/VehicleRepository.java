package com.ssafy.wtd.backend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehicleRepository {
    // user_id로 해당 사용자의 차량 전비(km/kWh) 조회
    Float getEfficiencyByUserId(@Param("userId") Long userId);
}

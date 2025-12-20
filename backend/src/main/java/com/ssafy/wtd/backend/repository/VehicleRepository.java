package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehicleRepository {
    // user_id로 해당 사용자의 차량 전비(km/kWh) 조회
    Float getEfficiencyByUserId(@Param("userId") Long userId);

    Vehicle findByUserId(@Param("userId") Long userId);

    // 모델 기반 자동 채움용: 최근 스펙 1건 가져오기
    Vehicle findSpecByModel(@Param("model") String model);

    int insert(Vehicle vehicle);

    int updateByUserId(Vehicle vehicle);

    int deleteByUserId(@Param("userId") Long userId);
}

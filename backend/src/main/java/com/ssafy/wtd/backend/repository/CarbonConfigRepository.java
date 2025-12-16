package com.ssafy.wtd.backend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CarbonConfigRepository {
    // 키값을 기준으로 float 값을 반환
    float findValueByKey(@Param("key") String key);
}

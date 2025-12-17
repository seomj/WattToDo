package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.CarbonConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarbonConfigRepository {
    // 최신 설정값 레코드를 통째로 조회
    CarbonConfig findLatestConfig();
}

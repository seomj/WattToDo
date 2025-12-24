package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.CarbonRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarbonRepository {
    void saveCarbonRecord(CarbonRecord carbonRecord);

    Float getTotalCarbonSavedByUserId(Long userId);
}

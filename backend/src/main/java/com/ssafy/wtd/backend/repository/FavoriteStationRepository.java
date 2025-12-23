package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.FavoriteStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteStationRepository {
    int insert(FavoriteStation favoriteStation);
    int delete(@Param("userId") Long userId, @Param("stationId") String stationId);
    FavoriteStation findByUserAndStation(@Param("userId") Long userId, @Param("stationId") String stationId);
    List<FavoriteStation> findByUserId(@Param("userId") Long userId);
}

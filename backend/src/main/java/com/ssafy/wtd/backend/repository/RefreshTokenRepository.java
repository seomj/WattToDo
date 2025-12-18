package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenRepository {

    void save(RefreshToken refreshToken);

    RefreshToken findByToken(@Param("token") String token);

    void deleteByUserId(@Param("userId") Long userId);
}

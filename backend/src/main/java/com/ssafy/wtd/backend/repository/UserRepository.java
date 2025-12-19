package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

    User findByUserId(@Param("userId") Long userId);

    User findByEmail(@Param("email") String email);

    int updateMyInfo(@Param("userId") Long userId, @Param("name") String name, @Param("nickname") String nickname);

    int disableUser(@Param("userId") Long userId);
}


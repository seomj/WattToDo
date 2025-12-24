package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

    User findByUserId(@Param("userId") Long userId);

    User findByEmail(@Param("email") String email);

    int updateMyInfo(@Param("userId") Long userId, @Param("email") String email, @Param("name") String name, @Param("nickname") String nickname);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    int insert(User user);

    int disableUser(@Param("userId") Long userId);

    int updateStatus(@Param("userId") Long userId, @Param("status") String status);
}

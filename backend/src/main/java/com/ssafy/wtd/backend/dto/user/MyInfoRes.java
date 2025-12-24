package com.ssafy.wtd.backend.dto.user;

import com.ssafy.wtd.backend.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyInfoRes {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float totalCarbonSaved;

    public static MyInfoRes from(User user, Float totalCarbonSaved) {
        return MyInfoRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalCarbonSaved(totalCarbonSaved)
                .build();
    }
}

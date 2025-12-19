package com.ssafy.wtd.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyInfoUpdateRes {
    private Long userId;
    private String name;
    private String nickname;
}

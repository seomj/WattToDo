package com.ssafy.wtd.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRes {
    private String accessToken;
    private String refreshToken;
    private String name;
}

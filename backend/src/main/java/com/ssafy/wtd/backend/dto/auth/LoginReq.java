package com.ssafy.wtd.backend.dto.auth;

import lombok.Getter;

@Getter
public class LoginReq {
    private String email;
    private String password;
}

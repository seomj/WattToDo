package com.ssafy.wtd.backend.dto.user;

import lombok.Getter;

@Getter
public class MyInfoUpdateReq {
    private String email;
    private String password;
    private String name;
    private String nickname;
}

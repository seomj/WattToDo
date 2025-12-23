package com.ssafy.wtd.backend.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupReq {
    private String email;
    private String password;
    private String name;
}

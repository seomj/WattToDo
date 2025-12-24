package com.ssafy.wtd.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoginRes {
    private String accessToken;
    private String refreshToken;
    private String name;
    private LocalDateTime createdAt;
    private Float totalCarbonSaved;
}

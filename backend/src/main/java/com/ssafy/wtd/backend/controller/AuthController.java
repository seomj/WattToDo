package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.auth.LoginReq;
import com.ssafy.wtd.backend.dto.auth.LoginRes;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenReq;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenRes;
import com.ssafy.wtd.backend.dto.auth.SignupReq;
import com.ssafy.wtd.backend.service.auth.AuthService;
import com.ssafy.wtd.backend.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshTokenRes refresh(@RequestBody RefreshTokenReq request) {
        String newAccessToken = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return new RefreshTokenRes(newAccessToken);
    }

    @PostMapping("/signup")
    public void signup(@RequestBody SignupReq request) {
        authService.signup(request);
    }
}

package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.auth.LoginReq;
import com.ssafy.wtd.backend.dto.auth.LoginRes;
import com.ssafy.wtd.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq request) {

        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. 인증 성공 → JWT 발급
        String accessToken =
                jwtUtil.generateAccessToken(
                        (org.springframework.security.core.userdetails.UserDetails)
                                authentication.getPrincipal()
                );

        // 3. 응답
        return new LoginRes(accessToken);
    }
}

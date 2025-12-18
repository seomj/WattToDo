package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.auth.LoginReq;
import com.ssafy.wtd.backend.dto.auth.LoginRes;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenReq;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenRes;
import com.ssafy.wtd.backend.model.RefreshToken;
import com.ssafy.wtd.backend.repository.RefreshTokenRepository;
import com.ssafy.wtd.backend.security.JwtUtil;
import com.ssafy.wtd.backend.service.auth.RefreshTokenService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq request) {

        // 1. 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // 2. Access Token 발급
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // 3. Refresh Token 발급
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 4. Refresh Token DB 저장 (MVP: Controller에서 처리)
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUserId(1L); // 임시 (DB 연동 시 교체)
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(
                LocalDateTime.now().plusDays(14)
        );

        refreshTokenRepository.save(tokenEntity);

        // 5. 응답
        return new LoginRes(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public RefreshTokenRes refresh(@RequestBody RefreshTokenReq request) {
        String newAccessToken =
                refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return new RefreshTokenRes(newAccessToken);
    }

}

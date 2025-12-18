package com.ssafy.wtd.backend.service.auth;

import com.ssafy.wtd.backend.model.RefreshToken;
import com.ssafy.wtd.backend.repository.RefreshTokenRepository;
import com.ssafy.wtd.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public String refreshAccessToken(String refreshToken) {

        // 1. DB에서 Refresh Token 조회
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (tokenEntity == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 2. 만료 체크
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        // 3. JWT에서 email 추출
        String email = jwtUtil.extractEmail(refreshToken);

        // 4. UserDetails 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 5. 새 Access Token 발급
        return jwtUtil.generateAccessToken(userDetails);
    }
}

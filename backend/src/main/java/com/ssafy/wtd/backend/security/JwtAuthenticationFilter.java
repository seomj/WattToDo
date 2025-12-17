package com.ssafy.wtd.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/api/v1/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Authorization 헤더 추출
        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer 토큰이 아니면 그냥 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ "Bearer " 제거 후 JWT 추출
        String jwt = authHeader.substring(7);

        // 3️⃣ JWT에서 사용자 식별 정보 추출 (보통 email)
        String email = jwtUtil.extractEmail(jwt);

        // 이미 인증된 경우 다시 인증하지 않음
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4️⃣ DB에서 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5️⃣ JWT 유효성 검증 (서명 + 만료 + 사용자 일치)
            if (jwtUtil.isTokenValid(jwt, userDetails)) {

                // 6️⃣ 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,      // principal
                                null,             // credentials (JWT라서 null)
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7️⃣ SecurityContext에 등록 (⭐ 핵심)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8️⃣ 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}

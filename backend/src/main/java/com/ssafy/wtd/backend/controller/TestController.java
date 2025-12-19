package com.ssafy.wtd.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// JWT 테스트용 Controller
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedApi() {
        return "JWT 인증 성공";
    }

    // ✅ JWT 인증 성공한 사용자만 접근 가능
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal UserDetails userDetails) {
        return "현재 로그인 사용자: " + userDetails.getUsername();
    }
}


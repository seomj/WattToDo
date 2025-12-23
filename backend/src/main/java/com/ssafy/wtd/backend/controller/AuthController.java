package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.auth.LoginReq;
import com.ssafy.wtd.backend.dto.auth.LoginRes;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenReq;
import com.ssafy.wtd.backend.dto.auth.RefreshTokenRes;
import com.ssafy.wtd.backend.model.RefreshToken;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.RefreshTokenRepository;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.security.JwtUtil;
import com.ssafy.wtd.backend.service.auth.RefreshTokenService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq request) {

        // 0. email로 사용자 직접 조회
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 1. DISABLED 사전 차단
        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 계정입니다."
            );
        }

        // 1.5 Legacy Password Migration (Plain Text -> BCrypt)
        // If password is NOT BCrypt encoded (e.g. plain text "1234"), matches() will likely fail or return false.
        // We check if it matches literally as a string.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // If it matches plain text, update it to BCrypt
            if (user.getPassword().equals(request.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.updatePassword(user.getUserId(), user.getPassword());
                // System.out.println("Migrated plain text password for user: " + user.getEmail());
            }
        }

        // 2. 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // 3. Access Token 발급
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // 4. Refresh Token 발급
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 5. Refresh Token DB 저장 (MVP: Controller에서 처리)
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUserId(1L); // 임시 (DB 연동 시 교체)
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(
                LocalDateTime.now().plusDays(14)
        );

        refreshTokenRepository.save(tokenEntity);

        // 6. 응답
        return new LoginRes(accessToken, refreshToken, user.getName(), user.getCreatedAt());
    }

    @PostMapping("/refresh")
    public RefreshTokenRes refresh(@RequestBody RefreshTokenReq request) {
        String newAccessToken =
                refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return new RefreshTokenRes(newAccessToken);
    }

    @PostMapping("/signup")
    public void signup(@RequestBody com.ssafy.wtd.backend.dto.auth.SignupReq request) {
        // 1. Check duplicate
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        // 2. Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt Password
        user.setStatus("ACTIVE");
        user.setRole("USER");
        user.setNickname(request.getName()); // Set default nickname to name

        // 3. Save
        userRepository.insert(user);
    }
}

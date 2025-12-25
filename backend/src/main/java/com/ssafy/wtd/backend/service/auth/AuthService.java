package com.ssafy.wtd.backend.service.auth;

import com.ssafy.wtd.backend.dto.auth.LoginReq;
import com.ssafy.wtd.backend.dto.auth.LoginRes;
import com.ssafy.wtd.backend.dto.auth.SignupReq;
import com.ssafy.wtd.backend.model.RefreshToken;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.RefreshTokenRepository;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final com.ssafy.wtd.backend.repository.CarbonRepository carbonRepository;

    /**
     * 회원가입 처리
     * 
     * @Transactional을 통해 DB 커밋을 자동으로 수행합니다.
     */
    @Transactional
    public void signup(SignupReq request) {
        // 1. 중복 체크
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        // 2. 객체 생성 및 암호화
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setRole("USER");

        // 3. 저장
        userRepository.insert(user);
    }

    /**
     * 로그인 처리
     * 
     * @Transactional을 통해 비밀번호 마이그레이션 및 토큰 저장을 안전하게 처리합니다.
     */
    @Transactional
    public LoginRes login(LoginReq request) {
        // 0. email로 사용자 직접 조회
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 1. DISABLED 사전 차단
        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 계정입니다.");
        }

        boolean passwordMigrated = false;

        // 1.5 Legacy Password Migration (Plain Text -> BCrypt)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // BCrypt 매칭 실패 - 평문 비밀번호 확인
            if (user.getPassword().equals(request.getPassword())) {
                // 평문 비밀번호 일치 → BCrypt로 마이그레이션
                String encodedPassword = passwordEncoder.encode(request.getPassword());
                user.setPassword(encodedPassword);
                userRepository.updatePassword(user.getUserId(), encodedPassword);
                passwordMigrated = true;
            } else {
                // BCrypt도 안 맞고, 평문도 안 맞으면 인증 실패
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "이메일 또는 비밀번호가 올바르지 않습니다.");
            }
        }

        UserDetails userDetails;

        if (passwordMigrated) {
            // 마이그레이션된 경우: AuthenticationManager를 거치지 않고 직접 UserDetails 생성
            // (AuthenticationManager는 DB의 새 BCrypt 비밀번호와 입력한 평문을 비교하므로 실패함)
            userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        } else {
            // BCrypt 비밀번호인 경우: 정상적으로 AuthenticationManager 사용
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            userDetails = (UserDetails) authentication.getPrincipal();
        }

        // 3. Access Token 발급
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // 4. Refresh Token 발급
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 5. Refresh Token DB 저장
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUserId(user.getUserId());
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(14));

        refreshTokenRepository.save(tokenEntity);

        // 6. CO2 감축량 조회
        Float totalCarbonSavedReq = carbonRepository.getTotalCarbonSavedByUserId(user.getUserId());
        float totalCarbonSaved = (totalCarbonSavedReq != null) ? totalCarbonSavedReq : 0.0f;

        // 7. 응답
        return new LoginRes(accessToken, refreshToken, user.getName(), user.getCreatedAt(), totalCarbonSaved);
    }
}

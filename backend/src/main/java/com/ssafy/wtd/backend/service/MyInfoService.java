package com.ssafy.wtd.backend.service;

import com.ssafy.wtd.backend.dto.user.MyInfoRes;
import com.ssafy.wtd.backend.dto.user.MyInfoRes;
import com.ssafy.wtd.backend.dto.user.MyInfoUpdateReq;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.RefreshTokenRepository;
import com.ssafy.wtd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final com.ssafy.wtd.backend.repository.CarbonRepository carbonRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public MyInfoRes getMyInfo(Long userId) {
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "유효하지 않은 사용자입니다."
            );
        }

        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 사용자입니다."
            );
        }

        Float totalCarbonSavedReq = carbonRepository.getTotalCarbonSavedByUserId(userId);
        float totalCarbonSaved = (totalCarbonSavedReq != null) ? totalCarbonSavedReq : 0.0f;

        return MyInfoRes.from(user, totalCarbonSaved);
    }

    public MyInfoRes updateMyInfo(Long userId, MyInfoUpdateReq req) {

        User user = userRepository.findByUserId(userId);
        if (user == null || "DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 사용자입니다."
            );
        }

        // 1. Email 중복 체크 (이메일이 변경된 경우)
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            User existing = userRepository.findByEmail(req.getEmail());
            if (existing != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "이미 사용 중인 이메일입니다."
                );
            }
        }

        // 2. 비밀번호 업데이트 (제공된 경우)
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            userRepository.updatePassword(userId, passwordEncoder.encode(req.getPassword()));
        }

        String updatedEmail = req.getEmail() != null ? req.getEmail() : user.getEmail();
        String updatedName = req.getName() != null ? req.getName() : user.getName();
        String updatedNickname = req.getNickname() != null ? req.getNickname() : user.getNickname();

        int updatedCount = userRepository.updateMyInfo(
                userId,
                updatedEmail,
                updatedName,
                updatedNickname
        );

        if (updatedCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "사용자 정보 수정에 실패했습니다."
            );
        }

        // Fetch the latest full info after update to ensure all fields (createdAt, role, etc.) are intact
        User updatedUser = userRepository.findByUserId(userId);
        
        Float totalCarbonSavedReq = carbonRepository.getTotalCarbonSavedByUserId(userId);
        float totalCarbonSaved = (totalCarbonSavedReq != null) ? totalCarbonSavedReq : 0.0f;

        return MyInfoRes.from(updatedUser, totalCarbonSaved);
    }

    public void deleteMyInfo(Long userId) {

        User user = userRepository.findByUserId(userId);
        if (user == null || "DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 사용자입니다."
            );
        }

        userRepository.disableUser(userId);

        // Refresh Token 무효화
        refreshTokenRepository.deleteByUserId(userId);
    }

    public boolean verifyPassword(Long userId, String plainPassword) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return false;
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }
}


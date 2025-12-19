package com.ssafy.wtd.backend.service;

import com.ssafy.wtd.backend.dto.user.MyInfoRes;
import com.ssafy.wtd.backend.dto.user.MyInfoUpdateReq;
import com.ssafy.wtd.backend.dto.user.MyInfoUpdateRes;
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

        return MyInfoRes.from(user);
    }

    public MyInfoUpdateRes updateMyInfo(Long userId, MyInfoUpdateReq req) {

        User user = userRepository.findByUserId(userId);
        if (user == null || "DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "비활성화된 사용자입니다."
            );
        }

        int updated = userRepository.updateMyInfo(
                userId,
                req.getName(),
                req.getNickname()
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "사용자 정보 수정에 실패했습니다."
            );
        }

        return new MyInfoUpdateRes(
                userId,
                req.getName(),
                req.getNickname()
        );
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


}


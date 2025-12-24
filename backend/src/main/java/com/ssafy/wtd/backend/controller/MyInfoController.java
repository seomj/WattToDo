package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.ApiRes;
import com.ssafy.wtd.backend.dto.user.MyInfoRes;
import com.ssafy.wtd.backend.dto.user.MyInfoRes;
import com.ssafy.wtd.backend.dto.user.MyInfoUpdateReq;
import com.ssafy.wtd.backend.dto.user.PasswordVerifyReq;
import com.ssafy.wtd.backend.security.CustomUserDetails;
import com.ssafy.wtd.backend.service.MyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyInfoController {

    private final MyInfoService myInfoService;

    @GetMapping("/myinfo")
    public ApiRes<MyInfoRes> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiRes.ok(
                myInfoService.getMyInfo(userDetails.getUserId())
        );
    }

    @PatchMapping("/myinfo")
    public ApiRes<MyInfoRes> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MyInfoUpdateReq request
    ) {
        return ApiRes.ok(
                myInfoService.updateMyInfo(
                        userDetails.getUserId(),
                        request
                )
        );
    }

    @DeleteMapping("/myinfo")
    public ApiRes<Void> deleteMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        myInfoService.deleteMyInfo(userDetails.getUserId());
        return ApiRes.ok(null);
    }

    @PostMapping("/myinfo/verify-password")
    public ApiRes<Boolean> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PasswordVerifyReq request
    ) {
        boolean isValid = myInfoService.verifyPassword(userDetails.getUserId(), request.getPassword());
        if (!isValid) {
            return ApiRes.fail("비밀번호가 일치하지 않습니다.");
        }
        return ApiRes.ok(true);
    }
}

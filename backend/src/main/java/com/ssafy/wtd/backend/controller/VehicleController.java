package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.ApiRes;
import com.ssafy.wtd.backend.dto.vehicle.*;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.security.CustomUserDetails;
import com.ssafy.wtd.backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    // 디버깅 위해 추가
    private final UserRepository userRepository;

    /**
     * 내 차량 조회
     */
    @GetMapping("/me")
    public ApiRes<VehicleRes> getMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiRes.ok(
                vehicleService.getMyVehicle(userDetails.getUserId())
        );
    }

    /**
     * 모델명으로 차량 스펙 상세 조회
     */
    @GetMapping("/spec")
    public ApiRes<VehicleRes> getVehicleSpec(@RequestParam String model) {
        return ApiRes.ok(vehicleService.getVehicleSpec(model));
    }

    /**
     * 차량 등록 (없으면 생성, 있으면 갱신)
     */
//    @PostMapping
//    public ApiRes<VehicleRes> registerMyVehicle(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestBody VehicleRegisterReq req
//    ) {
//        return ApiRes.ok(
//                vehicleService.registerMyVehicle(userDetails.getUserId(), req)
//        );
//    }

    @PostMapping
    public ApiRes<VehicleRes> registerMyVehicle(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            @RequestBody VehicleRegisterReq req
    ) {
        String email = principal.getUsername();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보가 없습니다.");
        }

        return ApiRes.ok(
                vehicleService.registerMyVehicle(user.getUserId(), req)
        );
    }

    /**
     * 차량 부분 수정
     */
    @PatchMapping
    public ApiRes<VehicleRes> updateMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody VehicleUpdateReq req
    ) {
        return ApiRes.ok(
                vehicleService.updateMyVehicle(userDetails.getUserId(), req)
        );
    }

    /**
     * 차량 삭제 (Hard)
     */
    @DeleteMapping
    public ApiRes<Void> deleteMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        vehicleService.deleteMyVehicle(userDetails.getUserId());
        return ApiRes.ok(null);
    }
}

package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.ApiRes;
import com.ssafy.wtd.backend.dto.vehicle.*;
import com.ssafy.wtd.backend.security.CustomUserDetails;
import com.ssafy.wtd.backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * 내 차량 조회
     */
    @GetMapping("/me")
    public ApiRes<VehicleRes> getMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiRes.ok(
                vehicleService.getVehicle(userDetails.getUserId()));
    }

    /**
     * 모델명으로 차량 스펙 상세 조회
     */
    @GetMapping("/spec")
    public ApiRes<VehicleRes> getVehicleSpec(@RequestParam String model) {
        return ApiRes.ok(vehicleService.getVehicleSpec(model));
    }

    @PostMapping
    public ApiRes<VehicleRes> registerMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody VehicleRegisterReq req) {
        return ApiRes.ok(
                vehicleService.registerVehicle(userDetails.getUserId(), req));
    }

    /**
     * 차량 부분 수정
     */
    @PatchMapping
    public ApiRes<VehicleRes> updateMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody VehicleUpdateReq req) {
        return ApiRes.ok(
                vehicleService.updateVehicle(userDetails.getUserId(), req));
    }

    /**
     * 차량 삭제 (Hard)
     */
    @DeleteMapping
    public ApiRes<Void> deleteMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        vehicleService.deleteVehicle(userDetails.getUserId());
        return ApiRes.ok(null);
    }
}

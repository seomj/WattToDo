package com.ssafy.wtd.backend.service.vehicle;

import com.ssafy.wtd.backend.dto.vehicle.*;
import com.ssafy.wtd.backend.model.Vehicle;
import com.ssafy.wtd.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /**
     * 내 차량 조회
     */
    public VehicleRes getMyVehicle(Long userId) {
        Vehicle vehicle = vehicleRepository.findByUserId(userId);
        return vehicle == null ? null : toRes(vehicle);
    }

    /**
     * 차량 등록
     * - 1인 1차량 정책
     * - 기존 차량 있으면 UPDATE
     * - 없으면 INSERT
     * - model 기반 자동 스펙 채움
     */
    public VehicleRes registerMyVehicle(Long userId, VehicleRegisterReq req) {
        if (req.getModel() == null || req.getModel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "model은 필수입니다.");
        }

        // 동일 모델 스펙 조회 (자동 채움용)
        Vehicle spec = vehicleRepository.findSpecByModel(req.getModel());

        Float efficiency = first(req.getEfficiency(), spec != null ? spec.getEfficiency() : null);
        Float batteryCapacity = first(req.getBatteryCapacity(), spec != null ? spec.getBatteryCapacity() : null);
        Float maxRange = first(req.getMaxRange(), spec != null ? spec.getMaxRange() : null);
        String dcChargeType = first(req.getDcChargeType(), spec != null ? spec.getDcChargeType() : null);
        String acChargeType = first(req.getAcChargeType(), spec != null ? spec.getAcChargeType() : null);

        // 스펙 DB에도 없고 사용자도 안 준 경우
        if (spec == null && (efficiency == null || batteryCapacity == null || maxRange == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "해당 차종 정보가 없어 efficiency, batteryCapacity, maxRange 입력이 필요합니다."
            );
        }

        Vehicle existing = vehicleRepository.findByUserId(userId);

        Vehicle vehicle = Vehicle.builder()
                .userId(userId)
                .model(req.getModel())
                .efficiency(efficiency)
                .batteryCapacity(batteryCapacity)
                .maxRange(maxRange)
                .dcChargeType(dcChargeType)
                .acChargeType(acChargeType)
                .build();

        if (existing == null) {
            vehicleRepository.insert(vehicle);
        } else {
            vehicleRepository.updateByUserId(vehicle);
        }

        return toRes(vehicleRepository.findByUserId(userId));
    }

    /**
     * 차량 부분 수정
     */
    public VehicleRes updateMyVehicle(Long userId, VehicleUpdateReq req) {

        Vehicle existing = vehicleRepository.findByUserId(userId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "등록된 차량이 없습니다.");
        }

        Vehicle vehicle = Vehicle.builder()
                .userId(userId)
                .model(req.getModel() != null ? req.getModel() : existing.getModel())
                .efficiency(req.getEfficiency() != null ? req.getEfficiency() : existing.getEfficiency())
                .batteryCapacity(req.getBatteryCapacity() != null ? req.getBatteryCapacity() : existing.getBatteryCapacity())
                .maxRange(req.getMaxRange() != null ? req.getMaxRange() : existing.getMaxRange())
                .dcChargeType(req.getDcChargeType() != null ? req.getDcChargeType() : existing.getDcChargeType())
                .acChargeType(req.getAcChargeType() != null ? req.getAcChargeType() : existing.getAcChargeType())
                .build();

        vehicleRepository.updateByUserId(vehicle);
        return toRes(vehicleRepository.findByUserId(userId));
    }

    /**
     * 차량 삭제 (Hard Delete)
     */
    public void deleteMyVehicle(Long userId) {
        vehicleRepository.deleteByUserId(userId);
    }

    /**
     * 모델명으로 차량 스펙 조회 (자동 채움용)
     */
    public VehicleRes getVehicleSpec(String model) {
        Vehicle spec = vehicleRepository.findSpecByModel(model);
        return spec == null ? null : toRes(spec);
    }

    /* ===========================
       private helper
       =========================== */

    private VehicleRes toRes(Vehicle v) {
        return new VehicleRes(
                v.getVehicleId(),
                v.getUserId(),
                v.getModel(),
                v.getEfficiency(),
                v.getBatteryCapacity(),
                v.getMaxRange(),
                v.getDcChargeType(),
                v.getAcChargeType(),
                v.getCreatedAt()
        );
    }

    private static <T> T first(T a, T b) {
        return a != null ? a : b;
    }
}

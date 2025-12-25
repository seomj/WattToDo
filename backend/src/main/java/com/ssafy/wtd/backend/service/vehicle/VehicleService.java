package com.ssafy.wtd.backend.service.vehicle;

import com.ssafy.wtd.backend.dto.vehicle.VehicleRegisterReq;
import com.ssafy.wtd.backend.dto.vehicle.VehicleRes;
import com.ssafy.wtd.backend.dto.vehicle.VehicleUpdateReq;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.model.Vehicle;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public VehicleRes getVehicle(Long userId) {
        Vehicle v = vehicleRepository.findByUserId(userId);
        if (v == null)
            return null;
        return toRes(v);
    }

    /**
     * 차량 등록 (Catalog Logic)
     * 1. 동일한 스펙(Model 등)의 차량이 있는지 조회
     * 2. 없으면 Vehicle 생성 (Catalog 추가)
     * 3. User의 vehicleId 업데이트 (Link)
     */
    @Transactional
    public VehicleRes registerVehicle(Long userId, VehicleRegisterReq req) {
        // 1. 기존 스펙 검색
        Vehicle specSearch = vehicleRepository.findSpecByModel(req.getModel());

        // 입력값 보완
        Float efficiency = first(req.getEfficiency(), specSearch != null ? specSearch.getEfficiency() : null);
        Float batteryCapacity = first(req.getBatteryCapacity(),
                specSearch != null ? specSearch.getBatteryCapacity() : null);
        Float maxRange = first(req.getMaxRange(), specSearch != null ? specSearch.getMaxRange() : null);
        String fastChargeType = first(req.getFastChargeType(),
                specSearch != null ? specSearch.getFastChargeType() : null);
        String slowChargeType = first(req.getSlowChargeType(),
                specSearch != null ? specSearch.getSlowChargeType() : null);

        Vehicle newVehicle = Vehicle.builder()
                .model(req.getModel())
                .efficiency(efficiency)
                .batteryCapacity(batteryCapacity)
                .maxRange(maxRange)
                .fastChargeType(fastChargeType)
                .slowChargeType(slowChargeType)
                .build();

        vehicleRepository.insert(newVehicle);
        userRepository.updateVehicleId(userId, newVehicle.getVehicleId());

        return toRes(newVehicle);
    }

    /**
     * 차량 '수정' -> 사실상 다른 Vehicle로 교체하거나 스펙 업데이트
     * Catalog 방식이므로, 기존 Vehicle 내용을 바꾸면 다른 사용자에게도 영향이 감.
     * 따라서 "수정" 요청이 들어오면:
     * 1. 새로운 Vehicle을 생성(또는 찾기)
     * 2. User가 그걸 가리키도록 변경 (Link 교체)
     */
    @Transactional
    public VehicleRes updateVehicle(Long userId, VehicleUpdateReq req) {
        User user = userRepository.findByUserId(userId);
        if (user == null)
            throw new IllegalArgumentException("User not found");

        // 내 차량 정보(기존 스펙 참고용)
        Vehicle existing = vehicleRepository.findByUserId(userId);

        Vehicle newVehicle = Vehicle.builder()
                .model(req.getModel())
                // 값이 없으면 기존 차량 스펙 유지
                .efficiency(req.getEfficiency() != null ? req.getEfficiency()
                        : (existing != null ? existing.getEfficiency() : null))
                .batteryCapacity(req.getBatteryCapacity() != null ? req.getBatteryCapacity()
                        : (existing != null ? existing.getBatteryCapacity() : null))
                .maxRange(req.getMaxRange() != null ? req.getMaxRange()
                        : (existing != null ? existing.getMaxRange() : null))
                .fastChargeType(req.getFastChargeType() != null ? req.getFastChargeType()
                        : (existing != null ? existing.getFastChargeType() : null))
                .slowChargeType(req.getSlowChargeType() != null ? req.getSlowChargeType()
                        : (existing != null ? existing.getSlowChargeType() : null))
                .build();

        vehicleRepository.insert(newVehicle);
        userRepository.updateVehicleId(userId, newVehicle.getVehicleId());

        return toRes(newVehicle);
    }

    @Transactional
    public void deleteVehicle(Long userId) {
        // User 연결 해제 (Vehicle 데이터는 Catalog로 남겨둠 - 히스토리용 or 타 유저 사용 가능성)
        // vehicleRepository.deleteByUserId(userId); // 기존 코드 삭제됨
        userRepository.updateVehicleId(userId, null);
    }

    @Transactional(readOnly = true)
    public VehicleRes getVehicleSpec(String model) {
        Vehicle spec = vehicleRepository.findSpecByModel(model);
        return spec == null ? null : toRes(spec);
    }

    /*
     * ===========================
     * Private Methods
     * ===========================
     */

    private VehicleRes toRes(Vehicle v) {
        return VehicleRes.builder()
                .vehicleId(v.getVehicleId())
                .model(v.getModel())
                .efficiency(v.getEfficiency())
                .batteryCapacity(v.getBatteryCapacity())
                .maxRange(v.getMaxRange())
                .fastChargeType(v.getFastChargeType())
                .slowChargeType(v.getSlowChargeType())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private static <T> T first(T a, T b) {
        return a != null ? a : b;
    }
}

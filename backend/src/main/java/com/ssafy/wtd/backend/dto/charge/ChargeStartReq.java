package com.ssafy.wtd.backend.dto.charge;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChargeStartReq {
    // 1. 공통 정보
    private Long userId;            // 현재 충전을 시작하는 사용자 ID

    // 2. 주유소 선택 및 위치 확인 관련
    private String stationId;         // 선택한 충전소 ID
    private Double userLatitude;    // 현재 사용자 위치 위도 (프론트에서 전송)
    private Double userLongitude;   // 현재 사용자 위치 경도 (프론트에서 전송)

    // 3. 입력 폼 관련
    private float targetKwh; // 목표 충전량
    private float startKwh; // 잔여 충전량 (시작 전 충전량)
    private float chargerCapacity;       // 충전기 용량
    // private LocalDateTime startTime; // BE에서 생성하는 것이 일반적이나 FE에서 전송 가능
}
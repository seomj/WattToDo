package com.ssafy.wtd.backend.service;

import com.ssafy.wtd.backend.dto.activity.ActivityRecommendReq;
import com.ssafy.wtd.backend.dto.activity.ActivityRecommendRes;
import com.ssafy.wtd.backend.dto.activity.ActivityRecommendRes.PlaceInfo;
import com.ssafy.wtd.backend.dto.activity.WeatherInfo;
import com.ssafy.wtd.backend.model.ChargeRecord;
import com.ssafy.wtd.backend.model.Vehicle;
import com.ssafy.wtd.backend.repository.ChargeRecordRepository;
import com.ssafy.wtd.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final VehicleRepository vehicleRepository;
    private final ChargeRecordRepository recordRepository;
    private final WeatherService weatherService;
    private final ChatClient chatClient;

    public ActivityRecommendRes getRecommendations(ActivityRecommendReq req) {
        // 1. 실시간 날씨 정보 획득 (위도/경도 기반)
        WeatherInfo weather = weatherService.getCurrentWeather(req.getLatitude(), req.getLongitude());

        // 2. o3-mini 모델을 사용한 추천 요청 (Fluent API)
        List<PlaceInfo> placeList = chatClient.prompt()
                .system("""
                        너는 전기차 충전 대기 시간 컨설턴트야. 사용자의 현재 상황을 분석해 최적의 장소 3곳을 추천해줘.

                        [논리적 제약 조건]
                        1. 충전 시간(%d분) 내에 모든 활동이 끝나야 함.
                        2. 이동 시간(%d분)은 편도 기준이므로, 왕복 이동 시간을 충전 시간에서 제외한 나머지가 실제 체류 시간임.
                        3. 현재 날씨 상태는 '%s'이므로 이를 고려해 실내/실외를 판단할 것.
                        4. 친환경 여부(%b)가 true라면, 친환경 인증이나 자연 친화적인 장소를 우선순위에 둘 것.

                        [출력 지침]
                        - 설명(description)은 사용자에게 친절한 말투로 작성할 것.
                        - imageUrl 필드에는 장소 성격에 맞는 키워드(cafe, park, restaurant, library 등)를 영문으로 작성할 것.
                        """.formatted(req.getChargingTime(), req.getTravelTime(), weather.getStatus(),
                        req.isEcoFriendly()))
                .user(u -> u.text("""
                        - 위치: 위도 {lat}, 경도 {lng}
                        - 목적: {purposes}
                        - 선호 장소: {locations}
                        - 선호 특징: {prefs}
                        - 인원: {count}명
                        - 대중교통 이용 가능 여부: {publicTrans}
                        """)
                        .params(Map.of(
                                "lat", req.getLatitude(),
                                "lng", req.getLongitude(),
                                "purposes", String.join(", ", req.getPurposes()),
                                "locations", String.join(", ", req.getLocations()),
                                "prefs", String.join(", ", req.getPreferences()),
                                "count", req.getPersonCount(),
                                "publicTrans", req.isPublicTransport())))
                // 3. o3-mini의 Structured Output 기능을 사용하여 List<PlaceInfo>로 자동 변환
                .call()
                .entity(new ParameterizedTypeReference<List<PlaceInfo>>() {
                });

        // 4. 최종 응답 DTO 빌드
        return ActivityRecommendRes.builder()
                .recommendations(placeList)
                .build();
    }

    /**
     * 예상 충전 시간 계산
     */
    private int calculateEstimatedTime(Long userId) {
        // 기반 데이터 추출
        Vehicle vehicle = vehicleRepository.findByUserId(userId);
        ChargeRecord active = recordRepository.selectActiveRecordByUserId(userId);

        if (vehicle == null || active == null)
            return 30; // 기본값

        float C = vehicle.getBatteryCapacity(); // 배터리 용량 (kWh)
        float S1 = active.getStartKwh() / C; // 잔여 비율
        float S2 = active.getTargetKwh() / C; // 목표 비율
        float P = active.getChargerCapacity(); // 충전기 용량

        // 공식: T = (C * (S2 - S1)) / P
        float hours = (C * (S2 - S1)) / P;
        return Math.round(hours * 60); // 분 단위 환산
    }
}

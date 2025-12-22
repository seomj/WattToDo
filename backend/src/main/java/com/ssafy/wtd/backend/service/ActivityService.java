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

    public String testAiConnection() {
        try {
            // 가장 단순한 형태의 호출 테스트
            return chatClient.prompt()
                    .user("안녕? 너는 지금 정상적으로 동작하고 있니? 간단하게 대답해줘.")
                    .call()
                    .content(); // JSON 변환 없이 문자열 그대로 반환
        } catch (Exception e) {
            // 에러 발생 시 상세 메시지 출력
            return "AI 호출 실패: " + e.getMessage();
        }
    }

    public ActivityRecommendRes getRecommendations(ActivityRecommendReq req) {
        try {
            // 1. 실시간 날씨 정보 획득
            WeatherInfo weather = weatherService.getCurrentWeather(req.getLatitude(), req.getLongitude());

            // 2. 파라미터 맵 생성 (Map.of의 10개 제한을 피하기 위해 Map.ofEntries 사용)
            Map<String, Object> promptParams = Map.ofEntries(
                    Map.entry("chargingTime", req.getChargingTime()),
                    Map.entry("travelTime", req.getTravelTime()),
                    Map.entry("weatherDesc", weather.getDescription()),
                    Map.entry("ecoFriendly", req.isEcoFriendly()),
                    Map.entry("lat", req.getLatitude()),
                    Map.entry("lng", req.getLongitude()),
                    Map.entry("purposes", String.join(", ", req.getPurposes())),
                    Map.entry("locations", String.join(", ", req.getLocations())),
                    Map.entry("prefs", String.join(", ", req.getPreferences())),
                    Map.entry("count", req.getPersonCount()),
                    Map.entry("publicTrans", req.isPublicTransport()));

            // 3. ChatClient 호출 (Fluent API)
            List<PlaceInfo> placeList = chatClient.prompt()
                    .user(u -> u.text("""
                            [역할 및 지침]
                            너는 전기차 충전 대기 시간 컨설턴트야. 사용자의 현재 상황을 분석해 최적의 장소 3개 이상 추천해줘.
                            설명(description)은 사용자에게 친절한 말투로 작성하고, 장소 설명(description)은 반드시 한국어로 작성해줘.
                            imageUrl 필드에는 장소 성격에 맞는 키워드(cafe, park, restaurant, library 등)를 영문으로 작성해줘.

                            [논리적 제약 조건]
                            1. 충전 시간({chargingTime}분) 내에 모든 활동이 끝나야 함.
                            2. 이동 시간({travelTime}분)은 편도 기준이므로, 왕복 이동 시간을 충전 시간에서 제외한 나머지가 실제 체류 시간임.
                            3. 현재 날씨 상태는 '{weatherDesc}'이므로 이를 고려해 실내/실외를 판단할 것.
                            4. 친환경 여부({ecoFriendly})가 true라면, 친환경 인증이나 자연 친화적인 장소를 우선순위에 둘 것.

                            [사용자 정보]
                            - 위치: 위도 {lat}, 경도 {lng} - 목적: {purposes} - 선호 장소: {locations}
                            - 선호 특징: {prefs} - 인원: {count}명 - 대중교통: {publicTrans}
                            """)
                            .params(promptParams))
                    .call()
                    .entity(new ParameterizedTypeReference<List<PlaceInfo>>() {
                    });

            // 4. 응답 반환
            return ActivityRecommendRes.builder()
                    .recommendations(placeList)
                    .build();

        } catch (Exception e) {
            // 에러 로깅
            System.err.println("AI 추천 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            // 사용자에게 친절한 에러 메시지와 함께 예외 전파
            throw new RuntimeException("AI 추천을 생성하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    /**
     * 외부(Controller)에서 호출하기 위한 Public 메서드
     */
    public int getEstimatedTime(Long userId) {
        return calculateEstimatedTime(userId);
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

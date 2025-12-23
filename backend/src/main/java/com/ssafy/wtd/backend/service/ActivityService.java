package com.ssafy.wtd.backend.service;

import com.ssafy.wtd.backend.dto.activity.ActivityRecommendReq;
import com.ssafy.wtd.backend.dto.activity.ActivityRecommendRes;
import com.ssafy.wtd.backend.model.ChargeRecord;
import com.ssafy.wtd.backend.model.Vehicle;
import com.ssafy.wtd.backend.repository.ChargeRecordRepository;
import com.ssafy.wtd.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final VehicleRepository vehicleRepository;
    private final ChargeRecordRepository recordRepository;
    private final WeatherService weatherService;
    private final KakaoLocalService kakaoLocalService;
    private final ChatClient chatClient;

    // 도보로 이동하는 데 걸리는 시간을 계산하기 위한 기준값
    private static final int WALKING_SPEED_M_PER_MIN = 80;

    public ActivityRecommendRes getRecommendations(ActivityRecommendReq req) {
        log.info("AI 추천 요청 수신 - [lat={}, lng={}]", req.getLatitude(), req.getLongitude());

        try {
            weatherService.getCurrentWeather(req.getLatitude(), req.getLongitude());

            // 1. 키워드 추출 및 스마트 매핑
            List<String> searchKeywords = new java.util.ArrayList<>();
            if (req.getLocations() != null)
                searchKeywords.addAll(req.getLocations());
            if (req.getPurposes() != null) {
                for (String p : req.getPurposes()) {
                    switch (p) {
                        case "업무/공부":
                            searchKeywords.addAll(List.of("노트북 하기 좋은 카페", "스터디카페", "도서관"));
                            break;
                        case "휴식":
                            searchKeywords.addAll(List.of("공원", "숲길", "서점", "카페"));
                            break;
                        case "식사":
                            searchKeywords.addAll(List.of("맛집", "식당"));
                            break;
                        case "운동":
                            searchKeywords.addAll(List.of("체육공원", "산책로", "볼링장"));
                            break;
                        case "쇼핑":
                            searchKeywords.addAll(List.of("백화점", "아울렛", "복합쇼핑몰"));
                            break;
                        case "관광":
                            searchKeywords.addAll(List.of("박물관", "미술관", "명소"));
                            break;
                        default:
                            searchKeywords.add(p);
                            break;
                    }
                }
            }
            if (searchKeywords.isEmpty())
                searchKeywords.addAll(List.of("카페", "맛집", "공원", "편의점", "명소"));

            // 2. 카카오 API 검색 및 데이터 정제
            List<Map<String, Object>> rawPlaceList = new java.util.ArrayList<>();
            java.util.Set<String> validNames = new java.util.HashSet<>();

            // [Final Strategy] 이동 수단에 따른 반경 전략분기
            // 대중교통 이용 가능: 4,000m (접근성 고려 확장)
            // 도보 이동 전용: 1,000m (근거리 확보)
            int calculatedRadius = req.isPublicTransport() ? 4000 : 1000;
            log.info("1단계 검색 반경 설정: {}m (대중교통 이용여부: {})",
                    calculatedRadius, req.isPublicTransport());

            for (String kw : searchKeywords) {
                List<Map<String, Object>> results = kakaoLocalService.searchPlaces(kw, req.getLatitude(),
                        req.getLongitude(), calculatedRadius);
                if (results != null) {
                    for (Map<String, Object> r : results) {
                        String name = (String) r.get("place_name");
                        if (validNames.add(name)) {
                            double pLat = Double.parseDouble(r.get("y").toString());
                            double pLng = Double.parseDouble(r.get("x").toString());
                            // 거리 계산
                            int dist = calculateHaversineDistance(req.getLatitude(), req.getLongitude(), pLat, pLng);
                            // 시간 계산 (ex. 도보 5분)
                            int time = (int) Math.ceil((double) dist / WALKING_SPEED_M_PER_MIN);

                            Map<String, Object> p = new java.util.HashMap<>();
                            p.put("name", name);
                            p.put("cat", r.get("category_name"));
                            p.put("addr", r.get("address_name"));
                            p.put("dist", dist);
                            p.put("time", time);
                            p.put("lat", pLat);
                            p.put("lng", pLng);
                            p.put("phone", r.get("phone"));
                            p.put("url", r.get("place_url"));
                            rawPlaceList.add(p);
                        }
                    }
                }
                if (rawPlaceList.size() >= 40)
                    break;
            }

            // [절대 규칙] 실존 장소가 하나도 없으면 AI를 호출하지 않고 즉시 응답 (할루시네이션 원천 차단)
            if (rawPlaceList.isEmpty()) {
                log.warn("주변 실존 장소 데이터가 0개입니다. AI 호출 없이 빈 결과를 반환합니다.");
                return ActivityRecommendRes.builder()
                        .recommendations(List.of())
                        .build();
            }

            log.info("수집된 유효 장소 수: {}", rawPlaceList.size());

            // 3. AI 프롬프트 구성
            StringBuilder context = new StringBuilder();
            context.append("다음 목록에 있는 실존 장소들만 사용해. **목록에 없는 이름을 단 하나라도 지어내면 절대 안 돼.**:\n");
            for (Map<String, Object> p : rawPlaceList) {
                context.append(
                        String.format("- %s (카테고리: %s, 거리: %dm, 시간: %d분, 주소: %s, lat: %s, lng: %s, 전화: %s, 사이트: %s)\n",
                                p.get("name"), p.get("cat"), p.get("dist"), p.get("time"), p.get("addr"), p.get("lat"),
                                p.get("lng"), p.get("phone"), p.get("url")));
            }

            // [Security & Validation] AI 분석 전 원본 데이터 로깅 (사용자 확인용)
            log.info("==================== [AI 분석 전 후보지 목록] ====================");
            if (rawPlaceList.isEmpty()) {
                log.info("검색된 후보지가 없습니다.");
            } else {
                for (int i = 0; i < rawPlaceList.size(); i++) {
                    Map<String, Object> p = rawPlaceList.get(i);
                    log.info("{}. [{}] 거리: {}m | 카테고리: {} | 주소: {} | 전화: {} | URL: {}",
                            (i + 1), p.get("name"), p.get("dist"),
                            p.get("cat"), p.get("addr"), p.get("phone"), p.get("url"));
                }
            }
            log.info("===============================================================");

            // 4. AI 호출 및 결과 합성
            log.info("AI 호출 중... (후보지: {}개)", rawPlaceList.size());

            String userPurposes = (req.getPurposes() == null || req.getPurposes().isEmpty()) ? "없음"
                    : String.join(", ", req.getPurposes());
            String userLocations = (req.getLocations() == null || req.getLocations().isEmpty()) ? "없음"
                    : String.join(", ", req.getLocations());

            // 날씨 정보 가져오기 (이미 fetch 했으나 context 주입 위해 다시 조회 가능)
            var weather = weatherService.getCurrentWeather(req.getLatitude(), req.getLongitude());

            String systemPrompt = """
                    당신은 전기차 충전 중인 사용자를 위한 활동 추천 전문가입니다.
                    제공된 '실존 장소 목록' 내에서만 추천을 생성해야 합니다.
                    **절대로 목록에 없는 장소를 지어내거나(Hallucination), 다른 지역의 장소를 가져오면 안 됩니다.**

                    추천 로직:
                    1. **사용자 의도 존중**: 사용자가 선택한 [목적: {purposes}]과 [장소: {locations}]을 최우선으로 고려하세요.
                       - 특히 목적이 '식사'이거나 장소가 '식당'인 경우, 카테고리가 '카페'나 '테마카페'(예: 애견카페)인 곳은 식사가 주 목적이 아니라면 제외하거나 후순위로 미루세요.
                    2. **최대한 많이 추천**: 제공된 목록에 사용자의 목적에 부합하는 장소가 있다면 **최소 5개에서 최대 10개까지** 충분히 추천하세요.
                    3. 순수 활동 시간(Net Stay) 계산: (전체 충전 시간 {chargingTime}분) - (추천 장소의 왕복 이동 시간 * 2).
                       - 순수 활동 시간이 부족한 장소는 제외하세요.
                    4. 이동 수단 ({transportType}) 고려:
                       - '대중교통' 모드인 경우, 버스나 지하철 접근성이 좋은 곳을 우선순위에 둡니다.
                       - '도보' 모드인 경우, 최대한 가까운 곳을 추천합니다.
                    5. 인원({personCount}명) 및 날씨({weather}) 고려:
                       - 날씨에 맞춰 실내/실외를 추천하되, 식당/카페 등 실내 장소는 비가 와도 적극 추천하세요.
                    6. 친환경({ecoFriendly}) 강조: 사용자가 친환경을 원한다면 자연 친화적인 곳을 우선하되, 다른 조건이 더 중요하다면 유연하게 판단하세요.

                    응답 형식:
                    - 반드시 아래 구조의 JSON 리스트로 응답하세요.
                    - 필드명: placeName, category, description, distanceMeter, travelTimeMin, isEcoFriendly, imageUrl, address, phone, placeUrl, latitude, longitude
                    - imageUrl: 'cafe', 'restaurant', 'park', 'shopping', 'culture', 'nature' 중 하나 선택
                    - address / latitude / longitude: 제공된 목록의 데이터를 정확히 입력하세요.
                    - phone / placeUrl: 제공된 목록의 데이터를 그대로 입력하세요.
                    - description(선정 이유)은 해당 장소가 사용자의 [목적]과 [선호도]에 왜 적합한지 구체적으로 작성하세요.
                    """;

            String userPrompt = "현재 날씨: {weather}\n사용자 목적: {purposes}\n선호 장소: {locations}\n개인 선호: {preferences}\n\n[실존 장소 목록]\n{context}";

            List<ActivityRecommendRes.PlaceInfo> aiResponse = chatClient.prompt()
                    .system(sp -> sp.text(systemPrompt)
                            .param("purposes", userPurposes)
                            .param("locations", userLocations)
                            .param("chargingTime", String.valueOf(req.getChargingTime()))
                            .param("personCount", String.valueOf(req.getPersonCount()))
                            .param("transportType", req.isPublicTransport() ? "대중교통 (버스/지하철 가능)" : "도보 전용")
                            .param("weather", weather.getDescription())
                            .param("ecoFriendly", req.isEcoFriendly() ? "중요" : "보통")
                            .param("preferences",
                                    (req.getPreferences() == null || req.getPreferences().isBlank()) ? "특별한 선호 없음"
                                            : req.getPreferences()))
                    .user(up -> up.text(userPrompt)
                            .param("weather", weather.getDescription() + " (" + weather.getTemp() + "°C)")
                            .param("purposes", userPurposes)
                            .param("locations", userLocations)
                            .param("preferences", req.getPreferences())
                            .param("context", context.toString()))
                    .call()
                    .entity(new ParameterizedTypeReference<List<ActivityRecommendRes.PlaceInfo>>() {
                    });

            // [최종 결과 로깅] AI가 선택한 최종 추천 목록 출력
            log.info("==================== [AI 최종 추천 결과] ====================");
            if (aiResponse == null || aiResponse.isEmpty()) {
                log.warn("AI가 적절한 장소를 추천하지 못했습니다.");
            } else {
                for (int i = 0; i < aiResponse.size(); i++) {
                    ActivityRecommendRes.PlaceInfo p = aiResponse.get(i);
                    log.info("{}. [{}] ({}) | {} | {}m ({}분) | 주소: {} | 위치: {}, {}",
                            (i + 1), p.getPlaceName(), p.getCategory(), p.getDescription(), p.getDistanceMeter(),
                            p.getTravelTimeMin(), p.getAddress(), p.getLatitude(), p.getLongitude());
                }
            }
            log.info("===============================================================");

            log.info("AI 추천 완료: {}개 장소 선정", (aiResponse != null ? aiResponse.size() : 0));

            return ActivityRecommendRes.builder()
                    .recommendations(aiResponse)
                    .build();

        } catch (Exception e) {
            log.error("AI 에러: {}", e.getMessage(), e);
            throw new RuntimeException("추천 서비스 일시 지연.", e);
        }
    }

    private int calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return (int) Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public int getEstimatedTime(Long userId) {
        Vehicle v = vehicleRepository.findByUserId(userId);
        ChargeRecord r = recordRepository.selectActiveRecordByUserId(userId);
        if (v == null || r == null)
            return 30;
        float cap = v.getBatteryCapacity();
        float time = (cap * (r.getTargetKwh() - r.getStartKwh()) / cap) / r.getChargerCapacity();
        return Math.round(time * 60);
    }
}

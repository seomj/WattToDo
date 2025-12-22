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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final VehicleRepository vehicleRepository;
    private final ChargeRecordRepository recordRepository;
    private final WeatherService weatherService;
    private final ChatClient chatClient;
    private final KakaoLocalService kakaoLocalService;

    private static final int WALKING_SPEED_M_PER_MIN = 80;

    public ActivityRecommendRes getRecommendations(ActivityRecommendReq req) {
        log.info("AI 추천 요청 수신 - [lat={}, lng={}]", req.getLatitude(), req.getLongitude());

        try {
            WeatherInfo weather = weatherService.getCurrentWeather(req.getLatitude(), req.getLongitude());

            // 1. 키워드 추출 및 스마트 매핑
            List<String> searchKeywords = new java.util.ArrayList<>();
            if (req.getLocations() != null)
                searchKeywords.addAll(req.getLocations());
            if (req.getPurposes() != null) {
                for (String p : req.getPurposes()) {
                    switch (p) {
                        case "업무":
                            searchKeywords.addAll(List.of("노트북 하기 좋은 카페", "스터디카페"));
                            break;
                        case "휴식":
                            searchKeywords.addAll(List.of("공원", "서점"));
                            break;
                        case "동네 산책":
                            searchKeywords.add("공원");
                            break;
                        case "맛집 탐방":
                            searchKeywords.add("맛집");
                            break;
                        default:
                            searchKeywords.add(p);
                            break;
                    }
                }
            }
            if (searchKeywords.isEmpty())
                searchKeywords.addAll(List.of("카페", "맛집", "공원", "편의점"));

            // 2. 카카오 API 검색 및 데이터 정제
            List<Map<String, Object>> rawPlaceList = new java.util.ArrayList<>();
            java.util.Set<String> validNames = new java.util.HashSet<>();

            for (String kw : searchKeywords) {
                List<Map<String, Object>> results = kakaoLocalService.searchPlaces(kw, req.getLatitude(),
                        req.getLongitude(), 2500);
                if (results != null) {
                    for (Map<String, Object> r : results) {
                        String name = (String) r.get("place_name");
                        if (validNames.add(name)) {
                            double pLat = Double.parseDouble(r.get("y").toString());
                            double pLng = Double.parseDouble(r.get("x").toString());
                            int dist = calculateHaversineDistance(req.getLatitude(), req.getLongitude(), pLat, pLng);
                            int time = (int) Math.ceil((double) dist / WALKING_SPEED_M_PER_MIN);

                            Map<String, Object> p = new java.util.HashMap<>();
                            p.put("name", name);
                            p.put("cat", r.get("category_name"));
                            p.put("addr", r.get("address_name"));
                            p.put("dist", dist);
                            p.put("time", time);
                            p.put("lat", pLat);
                            p.put("lng", pLng);
                            rawPlaceList.add(p);
                        }
                    }
                }
                if (rawPlaceList.size() >= 40)
                    break;
            }

            // 검색 결과 부족 시 확장
            if (rawPlaceList.size() < 10) {
                for (String fb : new String[] { "카페", "맛집", "공원" }) {
                    List<Map<String, Object>> results = kakaoLocalService.searchPlaces(fb, req.getLatitude(),
                            req.getLongitude(), 4000);
                    if (results != null) {
                        for (Map<String, Object> r : results) {
                            String name = (String) r.get("place_name");
                            if (validNames.add(name)) {
                                double pLat = Double.parseDouble(r.get("y").toString());
                                double pLng = Double.parseDouble(r.get("x").toString());
                                int dist = calculateHaversineDistance(req.getLatitude(), req.getLongitude(), pLat,
                                        pLng);
                                int time = (int) Math.ceil((double) dist / WALKING_SPEED_M_PER_MIN);

                                Map<String, Object> p = new java.util.HashMap<>();
                                p.put("name", name);
                                p.put("cat", r.get("category_name"));
                                p.put("addr", r.get("address_name"));
                                p.put("dist", dist);
                                p.put("time", time);
                                p.put("lat", pLat);
                                p.put("lng", pLng);
                                rawPlaceList.add(p);
                            }
                        }
                    }
                    if (rawPlaceList.size() >= 30)
                        break;
                }
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
                context.append(String.format("- %s (카테고리: %s, 거리: %dm, 시간: %d분, 주소: %s, lat: %s, lng: %s)\n",
                        p.get("name"), p.get("cat"), p.get("dist"), p.get("time"), p.get("addr"), p.get("lat"),
                        p.get("lng")));
            }

            Map<String, Object> params = Map.ofEntries(
                    Map.entry("chargingTime", req.getChargingTime()),
                    Map.entry("weather", weather.getDescription()),
                    Map.entry("context", context.toString()),
                    Map.entry("purposes", (req.getPurposes() == null || req.getPurposes().isEmpty()) ? "다양하게"
                            : String.join(", ", req.getPurposes())));

            // 4. AI 호출 (목표 5개 이상)
            List<PlaceInfo> aiResponse = chatClient.prompt()
                    .user(u -> u
                            .text("""
                                    너는 전기차 충전 대기 시간 가이드야.
                                    사용자의 대기 시간이 헛되지 않도록 **최대한 매력적인 5개 이상의 장소**를 추천해줘.

                                    **절대 규칙: 실존 데이터만 사용**
                                    1. 전달된 {context} 목록에 없는 장소 이름은 절대 언급하지 마. 지어내는 순간 안 돼.
                                    2. 목록에서 사용자의 목적({purposes})에 가장 잘 맞는 장소들을 골라.
                                    3. 목록의 거리, 시간, 좌표를 토씨 하나 틀리지 말고 그대로 출력해.

                                    [형식]
                                    - imageUrl: (cafe, restaurant, nature, park, book, shopping, gym, store) 중 택 1.
                                    - 모든 설명은 친절한 한국어.
                                    """)
                            .params(params))
                    .call()
                    .entity(new ParameterizedTypeReference<List<PlaceInfo>>() {
                    });

            // 5. 엄격한 사후 검증 (지어낸 이름 다시 한번 필터링)
            if (aiResponse != null) {
                aiResponse = aiResponse.stream()
                        .filter(p -> p.getPlaceName() != null && validNames.contains(p.getPlaceName().trim()))
                        .collect(Collectors.toList());
                log.info("검증 통과 추천 수: {}", aiResponse.size());
            }

            return ActivityRecommendRes.builder()
                    .recommendations(aiResponse != null ? aiResponse : List.of())
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

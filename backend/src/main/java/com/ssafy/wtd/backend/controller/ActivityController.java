package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.activity.ActivityRecommendReq;
import com.ssafy.wtd.backend.dto.activity.ActivityRecommendRes;
import com.ssafy.wtd.backend.dto.activity.WeatherInfo;
import com.ssafy.wtd.backend.service.ActivityService;
import com.ssafy.wtd.backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final WeatherService weatherService;
    private final ActivityService activityService;

    @GetMapping("/estimated-time/{userId}")
    public ResponseEntity<Map<String, Integer>> getEstimatedTime(@PathVariable Long userId) {
        int estimatedMin = activityService.getEstimatedTime(userId);
        // {"estimatedTime": 40} 형태로 응답
        return ResponseEntity.ok(Collections.singletonMap("estimatedTime", estimatedMin));
    }

    @PostMapping("/recommend")
    public ResponseEntity<ActivityRecommendRes> getRecommendations(@RequestBody ActivityRecommendReq req) {
        // 사용자가 입력한 데이터와 실시간 날씨를 조합해 AI 추천 실행
        ActivityRecommendRes result = activityService.getRecommendations(req);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/weather-test")
    public ResponseEntity<WeatherInfo> testWeather(
            @RequestParam(value = "lat", defaultValue = "37.5547") double lat,
            @RequestParam(value = "lon", defaultValue = "126.9707") double lon) {

        // 확실하지 않음: API 키가 유효하지 않거나 네트워크 문제 시 에러가 발생할 수 있습니다.
        WeatherInfo weatherInfo = weatherService.getCurrentWeather(lat, lon);

        return ResponseEntity.ok(weatherInfo);
    }
}

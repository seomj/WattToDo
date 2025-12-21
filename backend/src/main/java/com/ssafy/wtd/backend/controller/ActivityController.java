package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.activity.WeatherInfo;
import com.ssafy.wtd.backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final WeatherService weatherService;

    @GetMapping("/weather-test")
    public ResponseEntity<WeatherInfo> testWeather(
            @RequestParam(value = "lat", defaultValue = "37.5547") double lat,
            @RequestParam(value = "lon", defaultValue = "126.9707") double lon) {

        // 확실하지 않음: API 키가 유효하지 않거나 네트워크 문제 시 에러가 발생할 수 있습니다.
        WeatherInfo weatherInfo = weatherService.getCurrentWeather(lat, lon);

        return ResponseEntity.ok(weatherInfo);
    }
}

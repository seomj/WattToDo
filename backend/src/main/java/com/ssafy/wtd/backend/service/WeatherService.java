package com.ssafy.wtd.backend.service;

import com.ssafy.wtd.backend.dto.activity.WeatherInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherInfo getCurrentWeather(double lat, double lon) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=kr",
                lat, lon, apiKey
        );

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // 1. 날씨 설명 (weather list의 첫 번째 요소)
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
        String description = (String) weatherList.get(0).get("description");

        // 2. 기온 및 습도 (main object)
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        double temp = Double.parseDouble(main.get("temp").toString());
        int humidity = Integer.parseInt(main.get("humidity").toString());

        // 3. 바람 (wind object)
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        double windSpeed = Double.parseDouble(wind.get("speed").toString());

        // 4. 강수량 (rain 또는 snow object - 없을 수 있으므로 방어 로직 필수)
        double precipitation = 0.0;
        if (response.containsKey("rain")) {
            Map<String, Object> rain = (Map<String, Object>) response.get("rain");
            // 1시간 기준 강수량 추출 (필드명 '1h')
            if (rain.containsKey("1h")) {
                precipitation = Double.parseDouble(rain.get("1h").toString());
            }
        } else if (response.containsKey("snow")) {
            Map<String, Object> snow = (Map<String, Object>) response.get("snow");
            if (snow.containsKey("1h")) {
                precipitation = Double.parseDouble(snow.get("1h").toString());
            }
        }

        return WeatherInfo.builder()
                .description(description)
                .temp(temp)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .precipitation(precipitation)
                .build();
    }
}

package com.ssafy.wtd.backend.client;

import com.ssafy.wtd.backend.dto.external.EnvEvChargerRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class EnvEvChargerClient {

    private static final String BASE_URL =
            "https://apis.data.go.kr/B552584/EvCharger/getChargerInfo";

    @Value("${env.api.key}")
    private String serviceKey;

    private final RestTemplate restTemplate;

    /**
     * 환경부 EV 충전기 OpenAPI 호출
     *
     * @param pageNo     페이지 번호 (1부터)
     * @param numOfRows  페이지당 개수 (권장: 1000)
     * @param zcode      시도 코드 (예: 11 = 서울, null 가능)
     */
    public EnvEvChargerRes fetch(int pageNo, int numOfRows, String zcode) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("dataType", "JSON");

        // zcode는 선택 파라미터
        if (zcode != null && !zcode.isBlank()) {
            builder.queryParam("zcode", zcode);
        }

        String url = builder
                .build()
                .toUriString();

//        return restTemplate.getForObject(url, EnvEvChargerRes.class);

        // 디버깅용 임시 코드
        // ✅ 1단계: RAW 응답 확인
        String raw = restTemplate.getForObject(url, String.class);
        System.out.println("===== RAW API RESPONSE =====");
        System.out.println(raw);

        // ✅ 2단계: DTO 매핑 결과 확인
        EnvEvChargerRes res = restTemplate.getForObject(url, EnvEvChargerRes.class);
        System.out.println("===== MAPPED ITEMS SIZE =====");
        System.out.println(
                res == null ? "res=null" :
                        (res.getItems() == null ? "items=null" : res.getItems().size())
        );

        return res;
    }
}

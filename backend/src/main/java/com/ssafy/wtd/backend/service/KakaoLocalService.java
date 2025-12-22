package com.ssafy.wtd.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
public class KakaoLocalService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.rest-api.key:MISSING}")
    private String restApiKey;

    public KakaoLocalService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://dapi.kakao.com/v2/local")
                .build();
    }

    /**
     * 키워드로 주변 장소 검색
     */
    public List<Map<String, Object>> searchPlaces(String query, double lat, double lng, int radius) {
        if ("MISSING".equals(restApiKey) || restApiKey.startsWith("${")) {
            log.error("CRITICAL: Kakao API Key is not set in application.yml or environment variables!");
            return Collections.emptyList();
        }

        try {
            // 1. RAW byte[]로 수신하여 압축 여부 직접 확인
            ResponseEntity<byte[]> responseEntity = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("x", lng)
                            .queryParam("y", lat)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .build())
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .header("Accept-Encoding", "gzip, deflate, identity") // 명시적으로 지원 선언
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] responseBytes = responseEntity.getBody();

            if (responseBytes == null || responseBytes.length == 0) {
                log.warn("Kakao API returned an empty body for query: {}", query);
                return Collections.emptyList();
            }

            // 2. GZIP 압축 여부 확인 및 해제
            String json;
            if (isGZipped(responseBytes)) {
                json = decompressGzip(responseBytes);
            } else {
                json = new String(responseBytes, StandardCharsets.UTF_8);
            }

            // 3. Jackson을 이용해 Map으로 변환
            Map<String, Object> body = objectMapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });

            if (body != null && body.containsKey("documents")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
                return documents;
            } else if (body != null && body.containsKey("errorType")) {
                log.error("Kakao API Business Error: {} - {}", body.get("errorType"), body.get("message"));
            }

        } catch (Exception e) {
            log.error("Kakao Local API Failure [Query: {}]: {}", query, e.getMessage());
            // 에러 진단을 위한 헥사 덤프 로그 (첫 10바이트)
            try {
                ResponseEntity<byte[]> errRes = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/keyword.json").queryParam("query", query).build())
                        .header("Authorization", "KakaoAK " + restApiKey)
                        .retrieve().toEntity(byte[].class);
                byte[] data = errRes.getBody();
                if (data != null && data.length > 0) {
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < Math.min(data.length, 10); i++)
                        hex.append(String.format("%02X ", data[i]));
                    log.error("First 10 bytes of response: {}", hex.toString());
                }
            } catch (Exception ignored) {
            }
        }
        return Collections.emptyList();
    }

    private boolean isGZipped(byte[] data) {
        return (data.length >= 2 &&
                data[0] == (byte) GZIPInputStream.GZIP_MAGIC &&
                data[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    private String decompressGzip(byte[] data) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                GZIPInputStream gis = new GZIPInputStream(bis);
                InputStreamReader isr = new InputStreamReader(gis, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}

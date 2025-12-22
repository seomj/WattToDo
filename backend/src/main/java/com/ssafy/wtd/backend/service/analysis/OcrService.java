package com.ssafy.wtd.backend.service.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${clova.ocr.invoke.url}")
    private String invokeUrl;

    @Value("${clova.ocr.secret.key}")
    private String secretKey;

    public String callClovaOcr(MultipartFile file) throws Exception {
        // 1. 이미지 인코딩 및 확장자 추출
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String extension = getExtension(file.getOriginalFilename());

        // 2. 요청 바디 구성 (Map 사용)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "V2");
        requestBody.put("requestId", UUID.randomUUID().toString());
        requestBody.put("timestamp", System.currentTimeMillis());
        requestBody.put("lang", "ko");

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("format", extension);
        imageMap.put("name", "receipt");
        imageMap.put("data", base64Image);

        requestBody.put("images", Collections.singletonList(imageMap));

        // 3. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", secretKey);

        // 4. API 호출 (RestTemplate이 Map을 JSON으로 자동 변환)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForObject(invokeUrl, entity, String.class);
    }

    private String getExtension(String fileName) {
        return (fileName != null && fileName.contains("."))
                ? fileName.substring(fileName.lastIndexOf(".") + 1) : "png";
    }
}

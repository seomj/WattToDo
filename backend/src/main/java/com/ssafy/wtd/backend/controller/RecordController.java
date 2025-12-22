package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.charge.ChargeConfirmReq;
import com.ssafy.wtd.backend.dto.charge.ChargeConfirmRes;
import com.ssafy.wtd.backend.dto.charge.ChargeStartReq;
import com.ssafy.wtd.backend.dto.charge.ImageParsingRes;
import com.ssafy.wtd.backend.service.analysis.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/charge-records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    // 충전 시작 및 위치 확인 로직을 처리하는 API
    // POST /api/v1/charge-records/start
    @PostMapping("")
    public ResponseEntity<?> startCharging(@RequestBody ChargeStartReq request) {
        // 1. 요청 데이터를 서비스로 전달하여 로직 처리
        // 2. 서비스 결과에 따라 응답 결정
        try {
            recordService.processChargeStart(request);
            // 성공 시, 클라이언트에 충전 중 상태를 업데이트하도록 신호 전달
            return ResponseEntity.ok().body("{\"message\": \"Charging started successfully\"}");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // 위치 불일치 등 예외 발생 시 에러 메시지 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    // 영수증 이미지 업로드 & AI 파싱 API
    // POST /charge-records/{recordId}/receipt
    @PostMapping("/{recordId}/receipt")
    public ResponseEntity<ImageParsingRes> uploadReceiptAndParse(
            @PathVariable Long recordId,
            @RequestPart("file") MultipartFile imageFile) {

        ImageParsingRes response = recordService.processImageParsing(recordId, imageFile);

        return ResponseEntity.ok(response);
    }

    // AI 파싱 결과 확정(사용자 검증) API
    // POST /charge-records/{recordId}/confirm
    @PostMapping("/{recordId}/confirm")
    public ResponseEntity<ChargeConfirmRes> confirmParsedResult(
            @PathVariable Long recordId,
            @RequestBody ChargeConfirmReq request) { // 변경된 DTO 이름 적용

        ChargeConfirmRes response = recordService.confirmAndFinalizeRecord(recordId, request);

        return ResponseEntity.ok(response);
    }

    // ... 충전 완료, 기록 조회 등의 다른 API 메서드가 추가됩니다.
}
package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.dto.charge.ChargeConfirmReq;
import com.ssafy.wtd.backend.dto.charge.ChargeConfirmRes;
import com.ssafy.wtd.backend.dto.charge.ChargeStartReq;
import com.ssafy.wtd.backend.dto.charge.ImageParsingRes;
import com.ssafy.wtd.backend.model.ChargeRecord;
import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.ChargeRecordRepository;
import com.ssafy.wtd.backend.repository.UserRepository;
import com.ssafy.wtd.backend.service.analysis.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/charge-records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final ChargeRecordRepository chargeRecordRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyChargeRecords(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null)
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");

        List<ChargeRecord> records = chargeRecordRepository.findAllByUserId(user.getUserId());
        return ResponseEntity.ok(records);
    }

    // 충전 시작 API
    // POST /charge-records/start
    @PostMapping("/start")
    public ResponseEntity<Void> startCharging(
            @RequestBody ChargeStartReq request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        // 프론트에서 보낸 userId 대신 인증된 정보의 ID 사용 (보안 및 정확성)
        request.setUserId(user.getUserId());

        recordService.processChargeStart(request);
        return ResponseEntity.ok().build();
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
            @PathVariable("recordId") Long recordId,
            @RequestBody ChargeConfirmReq request,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null)
            return ResponseEntity.status(404).body(null);

        ChargeConfirmRes response = recordService.confirmAndFinalizeRecord(recordId, request, user.getUserId());

        return ResponseEntity.ok(response);
    }

    // 충전 강제 종료 API (이미지 인증 없이 취소)
    // DELETE /charge-records/{recordId}/cancel
    @DeleteMapping("/{recordId}/cancel")
    public ResponseEntity<?> cancelCharging(
            @PathVariable Long recordId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }

        // 충전 기록 조회
        ChargeRecord record = chargeRecordRepository.selectRecordById(recordId);
        if (record == null) {
            return ResponseEntity.status(404).body("충전 기록을 찾을 수 없습니다.");
        }

        // 본인의 기록인지 확인
        if (!record.getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        // 충전 기록 삭제
        chargeRecordRepository.deleteById(recordId);

        // 사용자 상태를 ACTIVE로 변경
        userRepository.updateStatus(user.getUserId(), "ACTIVE");

        return ResponseEntity.ok().body("충전이 취소되었습니다.");
    }

    // ... 충전 완료, 기록 조회 등의 다른 API 메서드가 추가됩니다.
}
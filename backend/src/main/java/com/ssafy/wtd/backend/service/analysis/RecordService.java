package com.ssafy.wtd.backend.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.wtd.backend.model.CarbonConfig;
import com.ssafy.wtd.backend.model.CarbonRecord;
import com.ssafy.wtd.backend.dto.charge.ChargeConfirmReq;
import com.ssafy.wtd.backend.dto.charge.ChargeConfirmRes;
import com.ssafy.wtd.backend.dto.charge.ChargeStartReq;
import com.ssafy.wtd.backend.dto.charge.ImageParsingRes;
import com.ssafy.wtd.backend.model.ChargeRecord;
import com.ssafy.wtd.backend.model.Station;
import com.ssafy.wtd.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final StationRepository stationRepository;
    private final ChargeRecordRepository recordRepository;
    private final VehicleRepository vehicleRepository; // 차량 정보 조회를 위해 주입
    private final CarbonRepository carbonRepository; // 탄소 저장용
    private final CarbonConfigRepository configRepository; // 설정값 조회용
    private final OcrService ocrService;

    // Geodesy 계산기 인스턴스 (Spring Bean으로 관리 가능하나, 여기서는 필드로 정의)
    // WGS84 타원체를 사용합니다. (GPS 시스템에서 표준으로 사용)
    private final GeodeticCalculator geoCalc = new GeodeticCalculator();
    private final Ellipsoid reference = Ellipsoid.WGS84;

    @Transactional
    public void processChargeStart(ChargeStartReq request) {
        // 사용자가 이미 충전 중인지 확인
        ChargeRecord activeRecord = recordRepository.selectActiveRecordByUserId(request.getUserId());
        if (activeRecord != null) {
            // 이미 CHARGING 상태의 기록이 존재하면, 요청을 거부하고 예외 발생
            throw new IllegalArgumentException("이미 다른 충전소에서 충전 중입니다.");
        }

        Station selectedStation = stationRepository.findById(request.getStationId());
        // 충전소 확인
        if (selectedStation == null) {
            throw new IllegalArgumentException("선택된 충전소를 찾을 수 없습니다.");
        }

        // 30m 허용 오차를 적용하여 위치 동일성 확인
        if (!isUserAtStation(selectedStation, request.getUserLatitude(), request.getUserLongitude())) {
            // 위치가 일치하지 않으면 예외 발생
            throw new IllegalArgumentException("현재 위치와 선택한 충전소가 30m 이내에 있지 않습니다. 다시 확인해 주세요.");
        }

        // 위치 확인 통과 시, 충전 기록 생성 및 저장
        ChargeRecord newRecord = createInitialRecord(request);
        recordRepository.insertRecord(newRecord);

        // TODO: 충전 상태 관리를 위한 Redis/Session 등의 로직 추가
    }

    /**
     * 충전소 위치와 사용자 위치가 허용 오차 범위(30m) 내에 있는지 확인하는 로직
     * - 외부 라이브러리를 사용하여 정밀한 GPS 거리 계산을 수행합니다.
     */
    private boolean isUserAtStation(Station station, Double userLat, Double userLon) {
        // 허용 오차 거리 상수 (30m)
        final double ALLOWED_DISTANCE_METERS = 30.0;

        // 1. 좌표 객체 생성
        GlobalCoordinates stationCoord = new GlobalCoordinates(station.getLat(), station.getLng());
        GlobalCoordinates userCoord = new GlobalCoordinates(userLat, userLon);

        // 2. Geodesy 라이브러리 호출하여 거리 계산 (거리 측정 결과의 'ellipsoidalDistance'를 사용)
        double distance = geoCalc.calculateGeodeticCurve(
                reference,
                stationCoord,
                userCoord
        ).getEllipsoidalDistance(); // 반환 값은 미터(m) 단위입니다.

        return distance <= ALLOWED_DISTANCE_METERS;
    }

    /**
     * DTO를 Model로 변환하고 충전 시작 초기 데이터를 기록합니다.
     * @param request 클라이언트로부터 받은 충전 시작 요청 DTO (ChargeStartReq)
     * @return DB 저장을 위한 ChargeRecord Model 객체
     */
    private ChargeRecord createInitialRecord(ChargeStartReq request) {
        ChargeRecord record = new ChargeRecord();

        // 1. 기본 정보 매핑
        record.setUserId(request.getUserId());
        record.setStationId(request.getStationId());

        // 2. 충전량 매핑
        record.setStartKwh(request.getStartKwh()); // 잔여량
        record.setTargetKwh(request.getTargetKwh()); // 목표량

        // 3. 초기 상태 및 시간 설정
        record.setStartTime(LocalDateTime.now()); // 서버 시간 기록
        record.setStatus("CHARGING"); // 초기 상태는 CHARGING

        return record;
    }

    @Transactional
    public ImageParsingRes processImageParsing(Long recordId, MultipartFile imageFile) {
        // 유효성 검사
        ChargeRecord record = recordRepository.selectRecordById(recordId);
        if (record == null) {
            // 기록이 존재하지 않음
            throw new NoSuchElementException("ID " + recordId + "에 해당하는 충전 기록을 찾을 수 없습니다.");
        }
        if (!"CHARGING".equals(record.getStatus())) {
            // 현재 CHARGING 상태가 아님 (이미 종료 또는 대기 중일 수 있음)
            throw new IllegalStateException("충전 기록 ID " + recordId + "는 현재 CHARGING 상태가 아닙니다.");
        }
        if (imageFile.isEmpty()) {
            // 이미지 파일이 없음
            throw new IllegalArgumentException("업로드할 이미지 파일이 비어 있습니다.");
        }

        try {
            // Clova OCR 호출
            String ocrResponse = ocrService.callClovaOcr(imageFile);
            String fullText = extractFullText(ocrResponse);

            // 데이터 추출 (정규식 패턴 활용)
            // 충전량 (예: 12.48KWh -> 12.48)
            float chargedKwh = parseValue(fullText, "충전량\\s*[:\\s]*([\\d.]+)", 0.0f);

            // 충전금액 (예: 4334원 -> 4334.0)
            float costRaw = parseValue(fullText, "(?:금액|충전금액)\\s*[:\\s]*([\\d,]+)", 0.0f);
            int chargingCost = Math.round(costRaw);

            // 충전소요시간 (예: 16:35 -> 16.58)
            String durationText = parseString(fullText, "충전시간\\s*[:\\s]*([\\d:]+)");

            // 응답 객체 생성
            ImageParsingRes.Parsed parsedData = new ImageParsingRes.Parsed();
            parsedData.setChargedKwh(chargedKwh);
            parsedData.setChargingCost(chargingCost);
            parsedData.setDurationText(durationText); // 예: 16:35

            ImageParsingRes.Data data = new ImageParsingRes.Data();
            data.setRecordId(recordId);
            data.setParsed(parsedData);

            ImageParsingRes response = new ImageParsingRes();
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("정보를 추출했습니다.");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * OCR 응답에서 모든 텍스트를 공백 기반으로 합치기
     */
    private String extractFullText(String ocrResponse) {
        try {
            // Jackson ObjectMapper를 사용하여 JSON을 Map으로 바로 변환
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(ocrResponse, Map.class);

            List<Map<String, Object>> images = (List<Map<String, Object>>) responseMap.get("images");
            List<Map<String, Object>> fields = (List<Map<String, Object>>) images.get(0).get("fields");

            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> field : fields) {
                sb.append(field.get("inferText")).append(" ");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String parseString(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private float parseValue(String text, String regex, float defaultValue) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String val = matcher.group(1).replace(",", ""); // 금액 쉼표 제거
            return Float.parseFloat(val);
        }
        return defaultValue;
    }

    @Transactional
    public ChargeConfirmRes confirmAndFinalizeRecord(Long recordId, ChargeConfirmReq request) {
        // 유효성 검사
        ChargeRecord record = recordRepository.selectRecordById(recordId);

        if (record == null) {
            // 기록이 존재하지 않음
            throw new NoSuchElementException("ID " + recordId + "에 해당하는 충전 기록을 찾을 수 없습니다.");
        }

        if (!"CHARGING".equals(record.getStatus())) {
            // 현재 CHARGING 상태가 아님 (이미 종료 또는 대기 중일 수 있음)
            throw new IllegalStateException("충전 기록 ID " + recordId + "는 현재 CHARGING 상태가 아닙니다.");
        }

        // 시간 계산 로직
        float durationMin = calculateDurationMinutes(request.getDurationText());

        LocalDateTime startTime = record.getStartTime();
        LocalDateTime endTime;

        if (startTime != null && durationMin > 0) {
            long secondsToAdd = (long) (durationMin * 60);
            endTime = startTime.plusSeconds(secondsToAdd);
        } else {
            // 시작 시각이 없거나 파싱 실패 시 현재 서버 시각 사용
            endTime = LocalDateTime.now();
        }

        // DB 업데이트
        recordRepository.updateRecord(
                recordId,                       // 1. recordId
                "COMPLETED",                    // 2. status
                request.getChargedKwh(),        // 3. chargedKwh (float)
                endTime,                        // 4. endTime (LocalDateTime)
                durationMin,                    // 5. durationMin (float)
                request.getChargingCost()       // 6. chargingCost (int)
        );

        // 탄소 절감량 계산 및 저장
        this.calculateAndSaveCarbonRecord(recordId, record.getUserId(), request.getChargedKwh());

        // 응답 생성
        ChargeConfirmRes.Data data = new ChargeConfirmRes.Data();
        data.setRecordId(recordId);
        data.setStationId(record.getStationId());
        data.setChargedKwh(request.getChargedKwh());
        data.setChargingCost(request.getChargingCost());
        data.setDurationMin(durationMin);

        ChargeConfirmRes response = new ChargeConfirmRes();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage("충전 기록이 저장되었습니다.");

        return response;
    }

    /**
     * 00:12:50 또는 16:35 포맷을 float 형태의 '분'으로 변환
     */
    private float calculateDurationMinutes(String durationText) {
        if (durationText == null || !durationText.contains(":")) return 0.0f;

        String[] parts = durationText.split(":");
        int totalSeconds = 0;

        try {
            if (parts.length == 2) { // mm:ss
                totalSeconds = (Integer.parseInt(parts[0]) * 60) + Integer.parseInt(parts[1]);
            } else if (parts.length == 3) { // hh:mm:ss
                totalSeconds = (Integer.parseInt(parts[0]) * 3600) + (Integer.parseInt(parts[1]) * 60) + Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException e) {
            return 0.0f;
        }

        return (float) totalSeconds / 60;
    }

    /**
     * 탄소 절감량을 계산하고 DB에 저장하는 내부 로직
     */
    private void calculateAndSaveCarbonRecord(Long recordId, Long userId, float chargedKwh) {
        // 1. 설정값 및 전비 조회
        float vehicleEfficiency = vehicleRepository.getEfficiencyByUserId(userId);
        CarbonConfig config = configRepository.findLatestConfig();
        if (config == null) {
            // 확실하지 않음: DB에 설정값이 하나도 없는 경우에 대한 방어 로직 (추측입니다)
            throw new IllegalStateException("탄소 계산 기준 설정(carbon_config)이 존재하지 않습니다.");
        }
        float avgFuelEff = config.getAvgFuelEfficiency();
        float gasCo2 = config.getGasolineCo2PerL();
        float evCo2 = config.getEvCo2PerKwh();

        // 2. 계산 공식 적용 (이미지 공식 기반)
        // (충전량 * 전비 / 내연기관 평균연비 * L당 CO2) - (충전량 * 전력 CO2 계수)
        float iceEmissions = (chargedKwh * vehicleEfficiency / avgFuelEff) * gasCo2;
        float evEmissions = chargedKwh * evCo2;
        float carbonSaved = iceEmissions - evEmissions;

        // 3. DB 저장
        CarbonRecord carbonRecord = new CarbonRecord();
        carbonRecord.setRecordId(recordId);
        carbonRecord.setUserId(userId);
        carbonRecord.setCarbonSaved(carbonSaved);

        carbonRepository.saveCarbonRecord(carbonRecord);
    }

}
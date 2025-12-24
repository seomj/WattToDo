package com.ssafy.wtd.backend.service.station;

import com.ssafy.wtd.backend.client.EnvEvChargerClient;
import com.ssafy.wtd.backend.dto.external.EnvEvChargerRes;
import com.ssafy.wtd.backend.dto.external.EnvEvChargerRes.Item;
import com.ssafy.wtd.backend.repository.station.ChargerRepository;
import com.ssafy.wtd.backend.repository.station.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingStationSyncService {

    private final EnvEvChargerClient envEvChargerClient;
    private final ChargingStationRepository chargingStationRepository;
    private final ChargerRepository chargerRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ChargingStationSyncService self; // Self-injection to use @Transactional proxy

    /**
     * 환경부 EV 충전소 / 충전기 데이터 동기화
     *
     * @param zcode 지역 코드 (ex. 11 = 서울)
     */
    public void refreshChargingStationData(String zcode) {

        int pageNo = 1;
        int numOfRows = 500;
        int maxPage = 3;

        log.info("[SYNC] Start EV charger sync (zcode={})", zcode);

        while (pageNo <= maxPage) {
            // [NETWORK I/O] 트랜잭션 외부에서 수행 (락 방지)
            EnvEvChargerRes res = envEvChargerClient.fetch(pageNo, numOfRows, zcode);

            if (res == null || res.isEmpty()) {
                break;
            }

            log.info("[SYNC] Page {} fetched, items={}", pageNo, res.getItems().size());

            // [DB I/O] 별도 트랜잭션으로 저장
            self.saveBatch(res.getItems());

            pageNo++;
        }

        log.info("[SYNC] Sync completed. total pages={}", pageNo - 1);
    }

    /**
     * 데이터 한 페이지(batch)를 트랜잭션으로 저장
     */
    @Transactional
    public void saveBatch(java.util.List<Item> items) {
        for (Item item : items) {
            // 1. 충전소 upsert
            chargingStationRepository.saveOrUpdate(
                    item.getStatId(),
                    item.getStatNm(),
                    item.getAddr(),
                    Double.parseDouble(item.getLat()),
                    Double.parseDouble(item.getLng()));

            // 2. 충전기 upsert
            chargerRepository.saveOrUpdate(
                    item.getStatId() + "_" + item.getChgerId(), // charger_id
                    item.getStatId(), // station_id
                    parseStatus(item.getStat()),
                    parsePowerType(item.getPowerType(), item.getChgerType()),
                    parseDetailedChargerType(item.getChgerType()));
        }
    }

    /**
     * 충전기 상태 코드 변환
     * 공공데이터 기준:
     * 1 = 통신이상
     * 2 = 사용가능
     * 3 = 충전중
     * 4 = 운영중지
     * 5 = 점검중
     */
    private int parseStatus(String stat) {
        if (stat == null)
            return 1;

        return switch (stat) {
            case "2" -> 0; // 정상 (사용 가능)
            case "3" -> 3; // 사용중
            case "5" -> 2; // 점검
            default -> 1; // 고장 / 통신이상
        };
    }

    /**
     * 충전기 타입 변환
     * 환경부 코드 기준
     */
    /**
     * 충전 분류 변환 (완속 / 급속)
     */
    private String parsePowerType(String powerType, String chgerType) {
        // 1. powerType 우선 확인 (급속/완속 키워드 포함 여부)
        if (powerType != null && !powerType.isEmpty()) {
            if (powerType.contains("급속"))
                return "급속";
            if (powerType.contains("완속"))
                return "완속";
        }

        // 2. powerType이 없거나 분류 불가 시 chgerType 활용 (기존 방식)
        if (chgerType == null)
            return "UNKNOWN";

        return switch (chgerType) {
            case "01", "02", "05", "06", "04", "09", "10" -> "급속";
            case "03", "07", "08" -> "완속";
            default -> "UNKNOWN";
        };
    }

    /**
     * 상세 충전기 타입 변환 (이미지 참고)
     */
    private String parseDetailedChargerType(String chgerType) {
        if (chgerType == null)
            return "알 수 없음";

        return switch (chgerType) {
            case "01" -> "DC차데모";
            case "02" -> "AC완속";
            case "03" -> "DC차데모+AC3상";
            case "04" -> "DC콤보";
            case "05" -> "DC차데모+DC콤보";
            case "06" -> "DC차데모+AC3상+DC콤보";
            case "07" -> "AC3상";
            case "08" -> "DC콤보(완속)";
            case "09" -> "NACS";
            case "10" -> "DC콤보+NACS";
            default -> "알 수 없음";
        };
    }
}

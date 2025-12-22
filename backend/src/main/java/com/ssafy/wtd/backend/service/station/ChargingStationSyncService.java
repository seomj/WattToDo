package com.ssafy.wtd.backend.service.station;

import com.ssafy.wtd.backend.client.EnvEvChargerClient;
import com.ssafy.wtd.backend.dto.external.EnvEvChargerRes;
import com.ssafy.wtd.backend.dto.external.EnvEvChargerRes.Item;
import com.ssafy.wtd.backend.repository.station.ChargerRepository;
import com.ssafy.wtd.backend.repository.station.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingStationSyncService {

    private final EnvEvChargerClient envEvChargerClient;
    private final ChargingStationRepository chargingStationRepository;
    private final ChargerRepository chargerRepository;

    /**
     * 환경부 EV 충전소 / 충전기 데이터 동기화
     *
     * @param zcode 지역 코드 (ex. 11 = 서울)
     */
    @Transactional
    public void refreshChargingStationData(String zcode) {

        int pageNo = 1;
        int numOfRows = 500;
        int maxPage = 3;

        log.info("[SYNC] Start EV charger sync (zcode={})", zcode);

        while (pageNo <= maxPage) {
            EnvEvChargerRes res = envEvChargerClient.fetch(pageNo, numOfRows, zcode);

            if (res == null || res.isEmpty()) {
                break;
            }

            log.info("[SYNC] Page {} fetched, items={}", pageNo, res.getItems().size());

            for (Item item : res.getItems()) {

                // =========================
                // 1. 충전소 upsert
                // =========================
                chargingStationRepository.saveOrUpdate(
                        item.getStatId(),
                        item.getStatNm(),
                        item.getAddr(),
                        Double.parseDouble(item.getLat()),
                        Double.parseDouble(item.getLng())
                );

                // =========================
                // 2. 충전기 upsert
                // =========================
                chargerRepository.saveOrUpdate(
                        item.getStatId() + "_" + item.getChgerId(), // charger_id
                        item.getStatId(),                            // station_id
                        item.getChgerNm(),
                        parseStatus(item.getStat()),
                        parseChargeType(item.getChgerType())
                );
            }

            pageNo++;
        }

        log.info("[SYNC] Sync completed. total pages={}", pageNo - 1);
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
        if (stat == null) return 1;

        return switch (stat) {
            case "2" -> 0; // 정상 (사용 가능)
            case "3" -> 3; // 사용중
            case "5" -> 2; // 점검
            default -> 1;  // 고장 / 통신이상
        };
    }

    /**
     * 충전기 타입 변환
     * 환경부 코드 기준
     */
    private String parseChargeType(String chgerType) {
        if (chgerType == null) return "UNKNOWN";

        return switch (chgerType) {
            case "01", "02" -> "DC";
            case "03", "04" -> "AC";
            case "05", "06" -> "FAST";
            default -> "UNKNOWN";
        };
    }
}

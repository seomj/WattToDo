package com.ssafy.wtd.backend.scheduler;

import com.ssafy.wtd.backend.service.station.ChargingStationSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StationScheduler {

    private final ChargingStationSyncService syncService;

    /**
     * 2분마다 서울 지역(zcode=11) 충전소/충전기 데이터 동기화
     */
    @Scheduled(fixedDelay = 120000)
    public void syncStationData() {
        log.info("[SCHEDULE] Starting periodic station data sync...");
        try {
            syncService.refreshChargingStationData("11");
            log.info("[SCHEDULE] Periodic sync completed successfully.");
        } catch (Exception e) {
            log.error("[SCHEDULE] Error occurred during periodic sync", e);
        }
    }
}

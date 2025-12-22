package com.ssafy.wtd.backend.controller;

import com.ssafy.wtd.backend.service.station.ChargingStationSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stations")
@RequiredArgsConstructor
public class StationAdminController {

    private final ChargingStationSyncService syncService;

    @PostMapping("/refresh")
    public void refresh(@RequestParam(required = false) String zcode) {
        syncService.refreshChargingStationData(zcode);
    }
}


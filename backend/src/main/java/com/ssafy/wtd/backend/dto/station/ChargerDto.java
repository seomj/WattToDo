package com.ssafy.wtd.backend.dto.station;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// 향후 상세 팝업, 관리자 화면 대비용
public class ChargerDto {

    private String chargerId;
    private String chargerName;

    private String chargeType;   // AC / DC
    private String status;
    private String statusLabel;

    private String lastUpdatedAt; // statUpdateDatetime
}

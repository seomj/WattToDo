package com.ssafy.wtd.backend.dto.station;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 충전기 카드 1개 단위 DTO
public class ChargerSummaryDto {

    private String chargerId;
    private String chargerName;

    private String chargeType;
    private String status;

    private String statusLabel;
    private String markerColor;

    // Service에서 최종 응답용 DTO를 만들 때만 사용하는 생성자
    public ChargerSummaryDto(
            String chargerId,
            String chargerName,
            String chargeType,
            String status,
            String statusLabel,
            String markerColor
    ) {
        this.chargerId = chargerId;
        this.chargerName = chargerName;
        this.chargeType = chargeType;
        this.status = status;
        this.statusLabel = statusLabel;
        this.markerColor = markerColor;
    }
}

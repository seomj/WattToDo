package com.ssafy.wtd.backend.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStationDetailDto {
    private String stationId;
    private String stationName;
    private String address;
}

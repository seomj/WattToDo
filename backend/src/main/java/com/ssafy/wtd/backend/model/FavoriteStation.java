package com.ssafy.wtd.backend.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteStation {
    private Long userId;
    private String stationId;
}

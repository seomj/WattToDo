package com.ssafy.wtd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarbonConfig {
    private Long configId;

    // DB 컬럼명과 매칭 (Snake Case -> Camel Case)
    private float gasolineCo2PerL;
    private float avgFuelEfficiency;
    private float evCo2PerKwh;

    private String configVersion;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}

package com.ssafy.wtd.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CarbonRecord {
    private Long carbonId;
    private Long recordId;
    private Long userId;
    private float carbonSaved;
    private LocalDateTime createdAt;
}

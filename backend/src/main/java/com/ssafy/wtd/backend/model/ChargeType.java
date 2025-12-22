package com.ssafy.wtd.backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChargeType {

    AC(1, "AC"),
    DC(2, "DC"),
    DC_COMBO(3, "DC"),
    DC_CHADEMO(4, "DC"),
    AC_3PHASE(5, "AC"),
    UNKNOWN(0, "UNKNOWN");

    private final int code;
    private final String displayType;

    public static ChargeType from(int code) {
        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isDc() {
        return "DC".equals(displayType);
    }

    public boolean isAc() {
        return "AC".equals(displayType);
    }
}

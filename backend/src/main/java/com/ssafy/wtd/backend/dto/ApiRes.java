package com.ssafy.wtd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiRes<T> {

    private boolean success;
    private T data;

    public static <T> ApiRes<T> ok(T data) {
        return new ApiRes<>(true, data);
    }
}

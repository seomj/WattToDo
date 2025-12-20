package com.ssafy.wtd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiRes<T> {

    private boolean success;
    private T data;
    private String message;

    // 성공 응답
    public static <T> ApiRes<T> ok(T data) {
        return new ApiRes<>(true, data, null);
    }

    // 실패 응답
    public static <T> ApiRes<T> fail(String message) {
        return new ApiRes<>(false, null, message);
    }
}

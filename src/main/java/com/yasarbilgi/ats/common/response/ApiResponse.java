package com.yasarbilgi.ats.common.response;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
}

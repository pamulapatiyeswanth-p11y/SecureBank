package com.securebank.accountservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message,T data){
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message){
        return ApiResponse.<T>builder()
                .success(true)
                .data(null)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message,T data){
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

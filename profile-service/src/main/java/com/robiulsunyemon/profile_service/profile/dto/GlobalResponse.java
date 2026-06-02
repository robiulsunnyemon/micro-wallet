package com.robiulsunyemon.profile_service.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {
    private int statusCode;
    private boolean success;
    private String message;
    private String path;
    private T data;
    private T errors;
    private LocalDateTime timestamp;


    public static <T> GlobalResponse<T> success(T data, String path) {
        return GlobalResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .success(true)
                .message("Success")
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> GlobalResponse<T> error(int statusCode, String message, String path) {
        return GlobalResponse.<T>builder()
                .statusCode(statusCode)
                .success(false)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
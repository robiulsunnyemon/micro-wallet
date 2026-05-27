package com.robiulsunyemon.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalResponse<T> {
    private int statusCode;
    private String message;
    private String path;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
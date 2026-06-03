package com.robiulsunyemon.auth_service.exceptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DuplicateException extends RuntimeException{
    private String message;
    private HttpStatus status;
}

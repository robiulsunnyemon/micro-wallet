package com.robiulsunyemon.auth_service.exceptions;
import com.robiulsunyemon.auth_service.dto.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptions {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> handleWalletException(ResourceNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .message("failed")
                .statusCode(ex.getStatus().value())
                .path(request.getRequestURI())
                .data(body)
                .build();
        return new ResponseEntity<>(response, ex.getStatus());
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex,HttpServletRequest request) {
        return buildResponse(request);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public  ResponseEntity<GlobalResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,HttpServletRequest request){
        Map<String, Object> body =  new LinkedHashMap<>();
        body.put("error", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .message("failed")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .data(body)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> buildResponse(HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Invalid phone number or password");
        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .message("failed")
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .data(body)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

}

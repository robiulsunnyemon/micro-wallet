package com.robiulsunyemon.auth_service.exceptions;

import com.robiulsunyemon.auth_service.dto.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptions {

    // ResourceNotFoundException (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request);
    }

    // BadRequestException (400 Bad Request)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GlobalResponse<Object>> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request);
    }

    // BadCredentialsException (401 Unauthorized)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalResponse<Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse("Invalid phone number or password", HttpStatus.UNAUTHORIZED, request);
    }

    // DisabledException (403 Forbidden / 401 Unauthorized)

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<GlobalResponse<Object>> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    // DataIntegrityViolationException (409 Conflict or 400 Bad Request)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorMessage = "Data integrity violation. The resource you are trying to create might already exist.";
        return buildErrorResponse(errorMessage, HttpStatus.CONFLICT, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    private ResponseEntity<GlobalResponse<Object>> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("error", message);

        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .message("failed")
                .statusCode(status.value())
                .path(request.getRequestURI())
                .data(errorBody)
                .build();

        return new ResponseEntity<>(response, status);
    }
}
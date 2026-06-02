package com.robiulsunyemon.wallet_service.wallet.exceptions;
import com.robiulsunyemon.wallet_service.wallet.dto.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
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



    // DataIntegrityViolationException (409 Conflict)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorMessage = "Data integrity violation. The resource you are trying to create might already exist.";
        return buildErrorResponse(errorMessage, HttpStatus.CONFLICT, request);
    }

    // Generic Exception (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // Common error response builder method using the updated GlobalResponse
    private ResponseEntity<GlobalResponse<Object>> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        // Create error details map (for nested error information)
        Map<String, String> errors = new HashMap<>();
        errors.put("error", message);

        // Build GlobalResponse using the factory method style
        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .statusCode(status.value())
                .success(false)
                .message(message)
                .path(request.getRequestURI())
                .data(null)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, status);
    }
}
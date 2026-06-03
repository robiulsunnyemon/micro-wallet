package com.robiulsunyemon.auth_service.controller;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@AllArgsConstructor
public class InternalController {
    private final AuthService authService;

    @GetMapping("/role/{userId}")
    public ResponseEntity<String> getUserRoleForInternal(@PathVariable Long userId) {
        AuthResponse response = authService.findById(userId);
        return ResponseEntity.ok(response.getRole().name());
    }
}

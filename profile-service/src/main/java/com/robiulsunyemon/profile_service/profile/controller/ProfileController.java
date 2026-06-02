package com.robiulsunyemon.profile_service.profile.controller;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import com.robiulsunyemon.profile_service.profile.dto.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProfileResponse>>> fetchAllProfiles(HttpServletRequest request){
        List<ProfileResponse> responses = profileService.fetchProfile();
        return buildSuccessResponse(
                responses,
                HttpStatus.OK,
                "Success",
                request.getRequestURI()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<GlobalResponse<ProfileResponse>> getProfileInfo(
            @RequestHeader(value = "userId", required = false) Long userId,
            HttpServletRequest request) {

        System.out.println("userId from profile service: "+userId);
        System.out.println("profile path from profile service: "+request.getRequestURL());

        if (userId == null) {
            return buildSuccessResponse(null, HttpStatus.UNAUTHORIZED,
                    "Missing userId header", request.getRequestURI());
        }

        ProfileResponse response = profileService.findProfileByUserId(userId);
        return buildSuccessResponse(response, HttpStatus.OK,
                "Success", request.getRequestURI());
    }

    @PatchMapping("/nid-submit")
    public ResponseEntity<GlobalResponse<String>> updateProfile(
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage,
            @RequestHeader(value = "userId", required = false) Long userId,
            HttpServletRequest request
    ) {
        if (userId == null) {
            return buildSuccessResponse(null, HttpStatus.UNAUTHORIZED,
                    "Missing userId header", request.getRequestURI());
        }

        try {
            // main thread-এ file bytes পড়ি, কারণ @Async thread-এ MultipartFile বন্ধ হয়ে যায়
            byte[] frontBytes = frontImage.getBytes();
            byte[] backBytes = backImage.getBytes();
            String frontContentType = frontImage.getContentType();
            String backContentType = backImage.getContentType();
            profileService.updateProfileWithNid(userId, frontBytes, frontContentType, backBytes, backContentType);
        } catch (Exception e) {
            return buildSuccessResponse(null, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read uploaded files", request.getRequestURI());
        }

        return buildSuccessResponse("NID images uploaded successfully. Processing started in background.",
                HttpStatus.ACCEPTED,
                "NID images uploaded successfully",
                request.getRequestURI());
    }

    @PostMapping("/upload-liveness")
    public ResponseEntity<GlobalResponse<String>> kycVerification(
            @RequestParam("selfie") MultipartFile selfie,
            @RequestHeader(value = "userId", required = false) Long userId,
            HttpServletRequest request
    ){
        if (userId == null) {
            return buildSuccessResponse(null, HttpStatus.UNAUTHORIZED,
                    "Missing userId header", request.getRequestURI());
        }

        try {
            byte[] selfieBytes = selfie.getBytes();
            profileService.kycVerificationWithNid(userId, selfieBytes);
        } catch (Exception e) {
            return buildSuccessResponse(null, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read uploaded file", request.getRequestURI());
        }

        return buildSuccessResponse("Liveness images uploaded successfully. Processing started in background.",
                HttpStatus.ACCEPTED,
                "Liveness verification started",
                request.getRequestURI());
    }

    private <T> ResponseEntity<GlobalResponse<T>> buildSuccessResponse(T data, HttpStatus status, String message, String path) {
        GlobalResponse<T> response = GlobalResponse.<T>builder()
                .statusCode(status.value())
                .success(true)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
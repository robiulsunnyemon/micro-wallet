package com.robiulsunyemon.profile_service.profile.controller;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public List<ProfileResponse> fetchAllProfiles(){
        return profileService.fetchProfile();
    }

    @GetMapping("/me")
    public ResponseEntity<Optional<ProfileResponse>> getProfileInfo(
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(profileService.findProfileByUserId(userId));
    }


    @PatchMapping("/nid-submit")
    public ResponseEntity<String> updateProfile(
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage,
            @RequestHeader(value = "userId", required = false) Long userId
    ) {

        profileService.updateProfileWithNid(userId, frontImage, backImage);
        return ResponseEntity.accepted().body("NID images uploaded successfully. Processing started in background.");
    }


    @PostMapping("/upload-liveness")
    public ResponseEntity<String> kycVerification(
            @RequestParam("selfie") MultipartFile selfie,
            @RequestHeader(value = "userId", required = false) Long userId
    ){
        profileService.kycVerificationWithNid(userId, selfie);
        return ResponseEntity.accepted().body("Liveness images uploaded successfully. Processing started in background.");
    }

}

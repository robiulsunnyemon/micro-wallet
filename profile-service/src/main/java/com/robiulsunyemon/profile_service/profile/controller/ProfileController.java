package com.robiulsunyemon.profile_service.profile.controller;
import com.robiulsunyemon.profile_service.profile.dto.ProfileRequest;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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


    @GetMapping("/{id}")
    public Optional<ProfileResponse> getProfileById(@PathVariable Long id){
        return profileService.findByProfileId(id);
    }

    @PatchMapping("/{userId}/upload-nid")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage) {

        try {
            ProfileResponse updatedProfile = profileService.updateProfileWithNid(userId, frontImage, backImage);
            return ResponseEntity.ok(updatedProfile);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping("/test/gemini")
    public ResponseEntity<String> testGemini(
            @RequestParam(value = "prompt", required = false) String prompt) {

        String result = profileService.testGeminiApi(prompt);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable Long id){
        return profileService.deleteProfileById(id);
    }
}

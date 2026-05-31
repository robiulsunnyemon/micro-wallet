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


    @GetMapping("/{id}")
    public Optional<ProfileResponse> getProfileById(@PathVariable Long id){
        return profileService.findByProfileId(id);
    }

    @GetMapping("/user/kyc/verification/{userId}")
    public Optional<ProfileResponse> getProfileByUserId(@PathVariable Long userId){
        return profileService.findProfileByUserId(userId);
    }

    @PatchMapping("/{userId}/upload-nid")
    public ResponseEntity<String> updateProfile(
            @PathVariable Long userId,
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage) {

        profileService.updateProfileWithNid(userId, frontImage, backImage);
        return ResponseEntity.accepted().body("NID images uploaded successfully. Processing started in background.");
    }

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable Long id){
        return profileService.deleteProfileById(id);
    }
}

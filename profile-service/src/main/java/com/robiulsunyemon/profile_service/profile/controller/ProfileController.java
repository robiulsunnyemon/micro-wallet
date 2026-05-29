package com.robiulsunyemon.profile_service.profile.controller;
import com.robiulsunyemon.profile_service.profile.dto.ProfileRequest;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable Long id){
        return profileService.deleteProfileById(id);
    }
}

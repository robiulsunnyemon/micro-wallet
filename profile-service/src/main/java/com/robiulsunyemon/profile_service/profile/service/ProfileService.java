package com.robiulsunyemon.profile_service.profile.service;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.dto.WalletCreatedMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProfileService {
    void createProfile(WalletCreatedMessage request);
    List<ProfileResponse> fetchProfile();
    ProfileResponse findByProfileId(Long id);
    ProfileResponse findProfileByUserId(Long id);
    String deleteProfileById(Long id);
    void updateProfileWithNid(Long userId, MultipartFile frontImage, MultipartFile backImage);
    void kycVerificationWithNid(Long userId, MultipartFile selfie);
}

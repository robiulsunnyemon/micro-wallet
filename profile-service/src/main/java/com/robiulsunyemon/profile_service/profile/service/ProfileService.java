package com.robiulsunyemon.profile_service.profile.service;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.dto.WalletCreatedMessage;
import java.util.List;


public interface ProfileService {
    void createProfile(WalletCreatedMessage request);
    List<ProfileResponse> fetchProfile();
    ProfileResponse findByProfileId(Long id);
    ProfileResponse findProfileByUserId(Long id);
    String deleteProfileById(Long id);
    void updateProfileWithNid(Long userId, byte[] frontBytes, String frontContentType, byte[] backBytes, String backContentType);
    void kycVerificationWithNid(Long userId, byte[] selfieBytes);
}

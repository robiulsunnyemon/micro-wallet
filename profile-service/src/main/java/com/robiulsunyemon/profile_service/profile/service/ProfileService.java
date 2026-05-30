package com.robiulsunyemon.profile_service.profile.service;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.dto.WalletCreatedMessage;

import java.util.List;
import java.util.Optional;

public interface ProfileService {
    void createProfile(WalletCreatedMessage request);
    List<ProfileResponse> fetchProfile();
    Optional<ProfileResponse> findByProfileId(Long id);
    String deleteProfileById(Long id);
}

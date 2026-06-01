package com.robiulsunyemon.profile_service.profile.service.impl;
import com.robiulsunyemon.profile_service.profile.dto.KycResultMessage;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycResultListener {

    private final ProfileRepository profileRepository;

    @RabbitListener(queues = "${rabbitmq.kyc-result-queue}")
    public void onKycResult(KycResultMessage result) {
        log.info("[KycResult] Received → userId={}, verified={}, distance={}",
                result.getUserId(), result.isVerified(), result.getDistance());

        Optional<ProfileEntity> profileOpt = profileRepository.findByUserId(result.getUserId());

        if (profileOpt.isEmpty()) {
            log.error("[KycResult] Profile not found for userId={}", result.getUserId());
            return;
        }

        ProfileEntity profile = profileOpt.get();

        if (result.isVerified()) {
            profile.setKycStatus(KycStatus.VERIFIED);
            log.info("[KycResult] ✅ userId={} → KycStatus.VERIFIED", result.getUserId());
        } else {
            profile.setKycStatus(KycStatus.FAILED);
            log.info("[KycResult] ❌ userId={} → KycStatus.FAILED", result.getUserId());
        }

        profileRepository.save(profile);
        log.info("[KycResult] Profile updated successfully for userId={}", result.getUserId());
    }
}
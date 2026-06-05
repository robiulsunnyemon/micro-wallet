package com.robiulsunyemon.profile_service.profile.service.impl;
import com.cloudinary.Cloudinary;
import com.robiulsunyemon.profile_service.profile.config.RabbitMQConfig;
import com.robiulsunyemon.profile_service.profile.dto.*;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.profile_service.profile.mapper.ProfileMapper;
import com.robiulsunyemon.profile_service.profile.repository.ProfileRepository;
import com.robiulsunyemon.profile_service.profile.service.AuditPublisherService;
import com.robiulsunyemon.profile_service.profile.service.NidService;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final NidService nidService;
    private final Cloudinary cloudinary;
    private final AuditPublisherService auditPublisherService;

    @RabbitListener(queues = "${rabbitmq.messaging.queue}")
    @Override
    public void createProfile(WalletCreatedMessage request) {
        try {

            if(profileRepository.findByUserId(request.getUserId()).isPresent()) {
                System.out.println("Successfully received Message from rabbitMq and create profile");
                RegistrationStatusMessage statusMessage=new RegistrationStatusMessage(true,request.getUserId());
                rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(),rabbitMQConfig.getRollBackRouting(),statusMessage);
                return ;
            }

            ProfileEntity profileEntity=new ProfileEntity();
            profileEntity.setUserId(request.getUserId());
            profileEntity.setWalletId(request.getWalletId());
            profileEntity.setKycStatus(KycStatus.PENDING);
            ProfileEntity savedEntity = profileRepository.save(profileEntity);
            System.out.println("Successfully received Message from rabbitMq and create profile");

            // Audit: Profile Creation Success
            Map<String, Object> auditNewValue = Map.of(
                    "userId", savedEntity.getUserId(),
                    "walletId", savedEntity.getWalletId(),
                    "kycStatus", savedEntity.getKycStatus().name()
            );
            auditPublisherService.publishAudit(
                    "PROFILE_CREATION", "SYSTEM", String.valueOf(savedEntity.getId()),
                    null, auditNewValue, "SUCCESS", "QUEUE_EVENT", "RabbitMQ_Listener", null
            );


            RegistrationStatusMessage statusMessage=new RegistrationStatusMessage(true,request.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(),rabbitMQConfig.getRollBackRouting(),statusMessage);

        } catch (Exception e) {

            // Audit: Profile Creation Failed
            auditPublisherService.publishAudit(
                    "PROFILE_CREATION", "SYSTEM", String.valueOf(request.getUserId()),
                    null, Map.of("userId", request.getUserId()), "FAILED", "QUEUE_EVENT", "RabbitMQ_Listener", e.getMessage()
            );

            RegistrationStatusMessage statusMessage=new RegistrationStatusMessage(false,request.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(),rabbitMQConfig.getRollBackRouting(),statusMessage);
            System.out.println("Error occur from profile service. No message received from wallet service. because: "+e);
            throw new e;
        }

    }

    @Override
    public List<ProfileResponse> fetchProfile() {
        return profileRepository.findAll().stream()
                .map(profileMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProfileResponse findByProfileId(Long id) {
        return profileRepository.findById(id).map(profileMapper::entityToResponse).orElseThrow(()->new ResourceNotFoundException("User is not found by profile id: "+id, HttpStatus.NOT_FOUND));
    }

    @Override
    public ProfileResponse findProfileByUserId(Long id) {
        return profileRepository.findByUserId(id)
                .map(profileMapper::entityToResponse).orElseThrow(()->new ResourceNotFoundException("User is not found by profile id: "+id, HttpStatus.NOT_FOUND));
    }

    @Override
    public String deleteProfileById(Long id) {
        if (profileRepository.existsById(id)) {
            profileRepository.deleteById(id);
            return "Profile deleted successfully";
        }
        return "Profile not found";
    }


    @Override
    @Async
    public void updateProfileWithNid(Long userId, byte[] frontBytes, String frontContentType, byte[] backBytes, String backContentType) {
        try {
            byte[] compressedFrontBytes = compressImage(frontBytes);
            byte[] compressedBackBytes = compressImage(backBytes);

            Map<?, ?> frontUploadResult = cloudinary.uploader().upload(compressedFrontBytes, com.cloudinary.utils.ObjectUtils.emptyMap());
            Map<?, ?> backUploadResult = cloudinary.uploader().upload(compressedBackBytes, com.cloudinary.utils.ObjectUtils.emptyMap());

            String frontUrl = (String) frontUploadResult.get("secure_url");
            String backUrl = (String) backUploadResult.get("secure_url");

            CompletableFuture.runAsync(() -> {
                try {
                    NidResponseDto nidResponseDto = nidService.parseNid(compressedFrontBytes, frontContentType, compressedBackBytes, backContentType);

                    Optional<ProfileEntity> profileEntityOpt = profileRepository.findByUserId(userId);
                    profileEntityOpt.ifPresent(entity -> {
                        Map<String, Object> oldKycState = Map.of("kycStatus", entity.getKycStatus().name());
                        entity.setNidNumber(nidResponseDto.getNidNumber());
                        entity.setNameEn(nidResponseDto.getNameEn());
                        entity.setNameBn(nidResponseDto.getNameBn());
                        entity.setDateOfBirth(LocalDate.parse(nidResponseDto.getDateOfBirth()).atStartOfDay());
                        entity.setAddress(nidResponseDto.getAddress());
                        entity.setNidFrontSide(frontUrl);
                        entity.setNidBackSide(backUrl);
                        entity.setKycStatus(KycStatus.NOT_VARIFIED);
                        ProfileEntity updatedEntity = profileRepository.save(entity);
                        // Audit: NID Upload and Profile Data Parse Success
                        Map<String, Object> newKycState = Map.of(
                                "nidNumber", updatedEntity.getNidNumber(),
                                "nameEn", updatedEntity.getNameEn(),
                                "kycStatus", updatedEntity.getKycStatus().name()
                        );
                        auditPublisherService.publishAudit(
                                "NID_DATA_UPDATE", String.valueOf(userId), String.valueOf(updatedEntity.getId()),
                                oldKycState, newKycState, "SUCCESS", "ASYNC_TASK", "Nid_OCR_Service", null
                        );

                    });
                } catch (Exception e) {
                    // Audit: Background NID Data Parse Failed
                    auditPublisherService.publishAudit(
                            "NID_DATA_UPDATE", String.valueOf(userId), null,
                            null, null, "FAILED", "ASYNC_TASK", "Nid_OCR_Service", e.getMessage()
                    );
                    System.err.println("Background NID processing failed: " + e.getMessage());
                    throw e;
                }
            });

            CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public void kycVerificationWithNid(Long userId, byte[] selfieBytes)

    {
        try {

            if(selfieBytes==null || selfieBytes.length == 0){
                System.out.println("kyc verification is failed. selfie is not found");
                return;
            }

            System.out.println("kyc verification function called");

            Map<?, ?> selfieImage = cloudinary.uploader().upload(selfieBytes, com.cloudinary.utils.ObjectUtils.emptyMap());
            String selfieUrl = (String) selfieImage.get("secure_url");

            CompletableFuture.runAsync(() -> {
                try {
                    Optional<ProfileEntity> profileEntityOpt = profileRepository.findByUserId(userId);
                    profileEntityOpt.ifPresent(entity -> {
                        KycRequestMessage kycRequestMessage = new KycRequestMessage(
                                userId,
                                selfieUrl,
                                entity.getNidFrontSide()
                        );

                        log.info("Sending KYC message: {}", kycRequestMessage);

                        rabbitTemplate.convertAndSend(
                                rabbitMQConfig.getKycPostExchange(),
                                rabbitMQConfig.getKycPostRoutingKey(),
                                kycRequestMessage
                        );

                        log.info("Message sent successfully");
                        // Audit: KYC Dispatch Success
                        Map<String, Object> auditNewValue = Map.of(
                                "selfieUrl", selfieUrl,
                                "nidFrontUrl", entity.getNidFrontSide()
                        );
                        auditPublisherService.publishAudit(
                                "KYC_VERIFICATION_DISPATCH", String.valueOf(userId), String.valueOf(entity.getId()),
                                null, auditNewValue, "SUCCESS", "ASYNC_TASK", "KYC_Dispatch_Handler", null
                        );
                    });
                } catch (Exception e) {
                    // Audit: KYC Dispatch Failed
                    auditPublisherService.publishAudit(
                            "KYC_VERIFICATION_DISPATCH", String.valueOf(userId), null,
                            null, null, "FAILED", "ASYNC_TASK", "KYC_Dispatch_Handler", e.getMessage()
                    );
                    System.err.println("Background NID processing failed: " + e.getMessage());
                    throw e;
                }
            });

            CompletableFuture.completedFuture(null);

        } catch (IOException e) {

            throw new e;
        }
    }

    public byte[] compressImage(byte[] imageBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new java.io.ByteArrayInputStream(imageBytes))
                .size(1200, 1200)
                .outputQuality(0.70)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

}

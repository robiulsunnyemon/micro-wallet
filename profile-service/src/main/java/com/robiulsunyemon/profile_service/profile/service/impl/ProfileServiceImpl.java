package com.robiulsunyemon.profile_service.profile.service.impl;
import com.cloudinary.Cloudinary;
import com.robiulsunyemon.profile_service.profile.config.RabbitMQConfig;
import com.robiulsunyemon.profile_service.profile.dto.*;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.profile_service.profile.mapper.ProfileMapper;
import com.robiulsunyemon.profile_service.profile.repository.ProfileRepository;
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


    @RabbitListener(queues = "${rabbitmq.queue}")
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
            RegistrationStatusMessage statusMessage=new RegistrationStatusMessage(true,request.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(),rabbitMQConfig.getRollBackRouting(),statusMessage);

        } catch (Exception e) {
            RegistrationStatusMessage statusMessage=new RegistrationStatusMessage(false,request.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(),rabbitMQConfig.getRollBackRouting(),statusMessage);
            System.out.println("Error occur from profile service. No message received from wallet service. because: "+e);
            throw new RuntimeException(e);
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
    public void updateProfileWithNid(Long userId, MultipartFile frontImage, MultipartFile backImage) {
        try {
            byte[] frontBytes = compressImage(frontImage);
            byte[] backBytes = compressImage(backImage);

            String frontContentType = frontImage.getContentType();
            String backContentType = backImage.getContentType();


            Map<?, ?> frontUploadResult = cloudinary.uploader().upload(frontBytes, com.cloudinary.utils.ObjectUtils.emptyMap());
            Map<?, ?> backUploadResult = cloudinary.uploader().upload(backBytes, com.cloudinary.utils.ObjectUtils.emptyMap());


            String frontUrl = (String) frontUploadResult.get("secure_url");
            String backUrl = (String) backUploadResult.get("secure_url");

            CompletableFuture.runAsync(() -> {
                try {
                    NidResponseDto nidResponseDto = nidService.parseNid(frontBytes, frontContentType, backBytes, backContentType);

                    Optional<ProfileEntity> profileEntityOpt = profileRepository.findByUserId(userId);
                    profileEntityOpt.ifPresent(entity -> {
                        entity.setNidNumber(nidResponseDto.getNidNumber());
                        entity.setNameEn(nidResponseDto.getNameEn());
                        entity.setNameBn(nidResponseDto.getNameBn());
                        entity.setDateOfBirth(LocalDate.parse(nidResponseDto.getDateOfBirth()).atStartOfDay());
                        entity.setAddress(nidResponseDto.getAddress());
                        entity.setNidFrontSide(frontUrl);
                        entity.setNidBackSide(backUrl);
                        entity.setKycStatus(KycStatus.NOT_VARIFIED);
                        profileRepository.save(entity);

                    });
                } catch (Exception e) {
                    System.err.println("Background NID processing failed: " + e.getMessage());
                }
            });

            CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image bytes", e);
        }
    }

    @Override
    public void kycVerificationWithNid(Long userId, MultipartFile selfie)

    {
        try {

            if(selfie==null){
                System.out.println("kyc verification is failed. selfie is not found");
            }

            byte[] selfieBytes = selfie.getBytes();
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
                    });
                } catch (Exception e) {
                    System.err.println("Background NID processing failed: " + e.getMessage());
                }
            });

            CompletableFuture.completedFuture(null);

        } catch (IOException e) {

            throw new RuntimeException("Failed to upload the selfie to Cloudinary.", e);
        }
    }

    public byte[] compressImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(1200, 1200)
                .outputQuality(0.70)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

}

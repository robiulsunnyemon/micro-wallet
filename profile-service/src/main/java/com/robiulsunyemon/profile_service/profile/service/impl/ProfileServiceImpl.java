package com.robiulsunyemon.profile_service.profile.service.impl;
import com.robiulsunyemon.profile_service.profile.config.RabbitMQConfig;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.dto.RegistrationStatusMessage;
import com.robiulsunyemon.profile_service.profile.dto.WalletCreatedMessage;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.mapper.ProfileMapper;
import com.robiulsunyemon.profile_service.profile.repository.ProfileRepository;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private ProfileRepository profileRepository;
    private ProfileMapper profileMapper;
    private RabbitTemplate rabbitTemplate;
    private RabbitMQConfig rabbitMQConfig;

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
    public Optional<ProfileResponse> findByProfileId(Long id) {
        return profileRepository.findById(id)
                .map(profileMapper::entityToResponse);
    }

    @Override
    public String deleteProfileById(Long id) {
        if (profileRepository.existsById(id)) {
            profileRepository.deleteById(id);
            return "Profile deleted successfully";
        }
        return "Profile not found";
    }
}

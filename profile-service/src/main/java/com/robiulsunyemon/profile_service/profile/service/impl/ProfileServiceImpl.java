package com.robiulsunyemon.profile_service.profile.service.impl;

import com.robiulsunyemon.profile_service.profile.dto.ProfileRequest;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.dto.UserCreatedMessage;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import com.robiulsunyemon.profile_service.profile.mapper.ProfileMapper;
import com.robiulsunyemon.profile_service.profile.repository.ProfileRepository;
import com.robiulsunyemon.profile_service.profile.service.ProfileService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileMapper profileMapper;


    @RabbitListener(queues = "${rabbitmq.queue}")
    @Override
    public void createProfile(UserCreatedMessage request) {
        ProfileEntity profileEntity=new ProfileEntity();
        profileEntity.setUserId(request.getUserId());
        ProfileEntity savedEntity = profileRepository.save(profileEntity);
        System.out.println("Successfully received Message from rabbitMq and create profile");

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

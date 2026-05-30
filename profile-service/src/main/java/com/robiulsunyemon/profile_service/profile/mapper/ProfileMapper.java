package com.robiulsunyemon.profile_service.profile.mapper;
import com.robiulsunyemon.profile_service.profile.dto.ProfileRequest;
import com.robiulsunyemon.profile_service.profile.dto.ProfileResponse;
import com.robiulsunyemon.profile_service.profile.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileEntity requestToEntity (ProfileRequest request){
        ProfileEntity newEntity = new ProfileEntity();
        newEntity.setUserId(request.getUserId());
        newEntity.setFirstName(request.getFirstName());
        newEntity.setLastName(request.getLastName());
        newEntity.setAddress(request.getAddress());
        newEntity.setNidNumber(request.getNidNumber());
        newEntity.setKycStatus(request.getKycStatus());
        return newEntity;
    }

    public ProfileResponse entityToResponse(ProfileEntity entity){
        ProfileResponse response = new ProfileResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setWalletId(entity.getWalletId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setAddress(entity.getAddress());
        response.setNidNumber(entity.getNidNumber());
        response.setKycStatus(entity.getKycStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

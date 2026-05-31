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
        newEntity.setNameEn(request.getNameEn());
        newEntity.setNameBn(request.getNameBn());
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
        response.setNameEn(entity.getNameEn());
        response.setNameBn(entity.getNameBn());
        response.setNidFrontSide(entity.getNidFrontSide());
        response.setNidBackSide(entity.getNidBackSide());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setAddress(entity.getAddress());
        response.setNidNumber(entity.getNidNumber());
        response.setKycStatus(entity.getKycStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

package com.robiulsunyemon.wallet_service.wallet.mapper;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import com.robiulsunyemon.wallet_service.wallet.entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletEntity requestToEntity (WalletRequest request){
        WalletEntity newEntity = new WalletEntity();
        newEntity.setUserId(request.getUserId());
        newEntity.setBalance(request.getBalance());
        newEntity.setCurrency(request.getCurrency());
        return newEntity;
    }

    public WalletResponse entityToResponse(WalletEntity entity){
        WalletResponse response = new WalletResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setBalance(entity.getBalance());
        response.setCurrency(entity.getCurrency());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

package com.robiulsunyemon.wallet_service.wallet.service;
import com.robiulsunyemon.wallet_service.wallet.dto.RegistrationStatusMessage;
import com.robiulsunyemon.wallet_service.wallet.dto.UserCreatedMessage;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import java.util.List;
import java.util.Optional;

public interface WalletService {
    void createWallet(UserCreatedMessage userCreatedMessage);
    void handleProfileRegistrationStatusUpdate(RegistrationStatusMessage statusMessage);
    List<WalletResponse> fetchWallet();
    Optional<WalletResponse> findByWalletId(Long id);
    WalletResponse findWalletByUserId(Long userId);
    String deleteWalletById(Long id);
}

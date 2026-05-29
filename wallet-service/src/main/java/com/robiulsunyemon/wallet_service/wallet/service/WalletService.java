package com.robiulsunyemon.wallet_service.wallet.service;
import com.robiulsunyemon.wallet_service.wallet.dto.UserCreatedMessage;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import java.util.List;
import java.util.Optional;

public interface WalletService {
    void createWallet(UserCreatedMessage userCreatedMessage);
    List<WalletResponse> fetchWallet();
    Optional<WalletResponse> findByWalletId(Long id);
    String deleteWalletById(Long id);
}

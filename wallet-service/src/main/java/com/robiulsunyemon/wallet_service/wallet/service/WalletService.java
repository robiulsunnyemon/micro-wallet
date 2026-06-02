package com.robiulsunyemon.wallet_service.wallet.service;
import com.robiulsunyemon.wallet_service.wallet.dto.*;
import com.robiulsunyemon.wallet_service.wallet.entity.FailureReasonType;

import java.util.List;
import java.util.Optional;

public interface WalletService {
    void createWallet(UserCreatedMessage userCreatedMessage);
    void handleProfileRegistrationStatusUpdate(RegistrationStatusMessage statusMessage);
    List<WalletResponse> fetchWallet();
    Optional<WalletResponse> findByWalletId(Long id);
    WalletResponse findWalletByUserId(Long userId);
    String deleteWalletById(Long id);
    void processWalletTransaction(TransactionEvent event);
    void sendRollbackMessage(TransactionEvent event, FailureReasonType reason);
}

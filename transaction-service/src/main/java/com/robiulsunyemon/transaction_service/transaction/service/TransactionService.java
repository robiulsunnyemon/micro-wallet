package com.robiulsunyemon.transaction_service.transaction.service;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionEvent;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    void createTransaction(Long userId, String role, TransactionRequest request, HttpServletRequest httpServletRequest);
    List<TransactionResponse> fetchTransaction();
    Optional<TransactionResponse> findByTransactionId(Long id);
    String deleteTransactionById(Long id);
    void updateTransactionStatus(TransactionEvent event);
}

package com.robiulsunyemon.transaction_service.transaction.service;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionEntity;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    void createTransaction(Long userId,String role,TransactionRequest request);
    List<TransactionResponse> fetchTransaction();
    Optional<TransactionResponse> findByTransactionId(Long id);
    String deleteTransactionById(Long id);
    void updateTransactionStatus(TransactionEntity event);
}

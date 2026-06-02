package com.robiulsunyemon.transaction_service.transaction.dto;
import com.robiulsunyemon.transaction_service.transaction.entity.CurrencyType;
import com.robiulsunyemon.transaction_service.transaction.entity.FailureReasonType;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionStatus;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionType;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Data
public class TransactionResponse {
    private Long id;
    private Long userId;
    private String txId;
    private String parentTxId;
    private Long senderUserId;
    private Long receiverUserId;
    private BigDecimal amount;
    private BigDecimal charge;
    private BigDecimal commission;
    private CurrencyType currency;
    private TransactionType txType;
    private TransactionStatus txStatus;
    private FailureReasonType failureReason;
    private String reference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

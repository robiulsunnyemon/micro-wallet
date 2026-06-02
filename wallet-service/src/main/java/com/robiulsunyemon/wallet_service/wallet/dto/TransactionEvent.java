package com.robiulsunyemon.wallet_service.wallet.dto;
import com.robiulsunyemon.wallet_service.wallet.entity.CurrencyType;
import com.robiulsunyemon.wallet_service.wallet.entity.FailureReasonType;
import com.robiulsunyemon.wallet_service.wallet.entity.TransactionStatus;
import com.robiulsunyemon.wallet_service.wallet.entity.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionEvent {
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

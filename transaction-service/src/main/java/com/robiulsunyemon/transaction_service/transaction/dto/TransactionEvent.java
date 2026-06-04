package com.robiulsunyemon.transaction_service.transaction.dto;
import com.robiulsunyemon.transaction_service.transaction.entity.FailureReasonType;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionStatus;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionEvent {
    private String txId;
    private Long senderUserId;
    private Long receiverUserId;
    private BigDecimal amount;
    private TransactionType txType;
    private String ipAddress;
    private BigDecimal charge;
    private BigDecimal commission;
    private TransactionStatus txStatus;
    private FailureReasonType failureReason;
}
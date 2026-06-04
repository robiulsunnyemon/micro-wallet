package com.robiulsunyemon.fraud_detection_service.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
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
package com.robiulsunyemon.transaction_service.transaction.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String txId;

    private String parentTxId;

    private Long senderUserId;

    private Long receiverUserId;

    private BigDecimal amount;

    private BigDecimal charge;

    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Enumerated(EnumType.STRING)
    private TransactionType txType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus txStatus;

    @Enumerated(EnumType.STRING)
    private FailureReasonType failureReason;

    private String reference;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void setCreatedDate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void setUpdatedDate(){
        updatedAt=LocalDateTime.now();
    }
}

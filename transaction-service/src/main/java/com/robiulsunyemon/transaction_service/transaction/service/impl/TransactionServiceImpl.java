package com.robiulsunyemon.transaction_service.transaction.service.impl;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.entity.*;
import com.robiulsunyemon.transaction_service.transaction.exceptons.ResourceNotFoundException;
import com.robiulsunyemon.transaction_service.transaction.mapper.TransactionMapper;
import com.robiulsunyemon.transaction_service.transaction.repository.TransactionRepository;
import com.robiulsunyemon.transaction_service.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionMapper transactionMapper;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Override
    @Transactional
    public void createTransaction(Long userId, String roleStr, TransactionRequest request) {
        log.info("Initiating transaction for user id: {} with type: {}", userId, request.getTxType());

        Role receiverRole = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                String cleanRole = roleStr.replace("ROLE_", "").toUpperCase().trim();
                receiverRole = Role.valueOf(cleanRole);
            } catch (IllegalArgumentException e) {
                log.error("Invalid role passed from token: {}", roleStr);
                throw new IllegalArgumentException("Invalid user role provided in request context");
            }
        } else {
            log.warn("Role string is missing or empty for userId: {}", userId);
        }

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setUserId(userId);

        BigDecimal amount = request.getAmount();
        TransactionType txType = request.getTxType();
        BigDecimal limitAmount = new BigDecimal("25000.00");
        boolean isAboveLimit = amount.compareTo(limitAmount) > 0;


        BigDecimal charge = calculateCharge(txType, isAboveLimit);
        transactionEntity.setCharge(charge);

        BigDecimal commission = calculateCommissionFromCharge(txType, charge, receiverRole);
        transactionEntity.setCommission(commission);

        transactionEntity.setParentTxId(request.getParentTxId());
        transactionEntity.setTxId(generateUniqueTxId());
        transactionEntity.setSenderUserId(userId);
        transactionEntity.setReceiverUserId(request.getReceiverUserId());
        transactionEntity.setAmount(amount);
        transactionEntity.setTxType(txType);
        transactionEntity.setCurrency(CurrencyType.BDT);
        transactionEntity.setTxStatus(TransactionStatus.PENDING);
        transactionEntity.setReference(request.getReference());


        TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);
        log.info("Transaction saved in database with ID: {} and Status: PENDING", savedTransaction.getTxId());


        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedTransaction);
            log.info("Successfully published transaction message to RabbitMQ exchange: {}", exchange);
        } catch (Exception e) {
            log.error("Failed to send transaction message to RabbitMQ for TxId: {}", savedTransaction.getTxId(), e);
            throw new RuntimeException("Message queue dispatch failed. Transaction rolled back.", e);
        }
    }


    private BigDecimal calculateCharge(TransactionType txType, boolean isAboveLimit) {
        if (isAboveLimit) {
            return switch (txType) {
                case SEND_MONEY -> new BigDecimal("10.00");
                case CASH_OUT -> new BigDecimal("20.00");
                case MOBILE_RECHARGE -> new BigDecimal("2.00");
                case PAYMENT -> new BigDecimal("5.00");
                default -> BigDecimal.ZERO;
            };
        } else {
            return switch (txType) {
                case SEND_MONEY -> new BigDecimal("5.00");
                case CASH_OUT -> new BigDecimal("10.00");
                case MOBILE_RECHARGE -> new BigDecimal("1.00");
                case PAYMENT -> new BigDecimal("3.00");
                default -> BigDecimal.ZERO;
            };
        }
    }


    private BigDecimal calculateCommissionFromCharge(TransactionType txType, BigDecimal charge, Role receiverRole) {
        if (charge.compareTo(BigDecimal.ZERO) <= 0 || receiverRole == null) {
            return BigDecimal.ZERO;
        }


        if (txType == TransactionType.CASH_OUT && receiverRole == Role.AGENT) {
            BigDecimal agentShareRate = new BigDecimal("0.40"); // ৪০%
            return charge.multiply(agentShareRate).setScale(2, RoundingMode.HALF_UP);
        }


        if (txType == TransactionType.PAYMENT && receiverRole == Role.MERCHANT) {
            BigDecimal merchantShareRate = new BigDecimal("0.50"); // ৫০%
            return charge.multiply(merchantShareRate).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<TransactionResponse> fetchTransaction() {
        log.info("Fetching all transactions from database");
        List<TransactionEntity> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transactionMapper::entityToResponse)
                .toList();
    }

    @Override
    public Optional<TransactionResponse> findByTransactionId(Long id) {
        return Optional.empty();
    }

    @Override
    public String deleteTransactionById(Long id) {
        return "";
    }

    @Override
    @Transactional
    public void updateTransactionStatus(TransactionEntity event) {
        log.info("Updating final transaction status in database for TxId: {}", event.getTxId());

        TransactionEntity transaction = transactionRepository.findByTxId(event.getTxId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for TxId: " + event.getTxId(), HttpStatus.NOT_FOUND));

        try {

            TransactionStatus finalStatus = TransactionStatus.valueOf(event.getTxStatus().name());
            transaction.setTxStatus(finalStatus);


            if (finalStatus == TransactionStatus.FAILED && event.getFailureReason() != null) {
                log.warn("Transaction {} failed due to: {}", event.getTxId(), event.getFailureReason());

                try {
                    transaction.setFailureReason(event.getFailureReason());
                } catch (IllegalArgumentException e) {
                    log.error("Unknown failure reason received: {}. Falling back to SYSTEM_ERROR", event.getFailureReason());
                    transaction.setFailureReason(FailureReasonType.SYSTEM_ERROR);
                }
            }

            transactionRepository.save(transaction);
            log.info("Transaction TxId: {} has been finalized to status: {}", event.getTxId(), finalStatus);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status value received from wallet service: {}", event.getTxStatus());
        }
    }

    private String generateUniqueTxId() {
        return "TX" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
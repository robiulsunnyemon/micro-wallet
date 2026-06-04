package com.robiulsunyemon.transaction_service.transaction.service.impl;
import com.robiulsunyemon.transaction_service.transaction.clients.WalletClient;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionEvent;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.entity.*;
import com.robiulsunyemon.transaction_service.transaction.exceptons.BadRequestException;
import com.robiulsunyemon.transaction_service.transaction.exceptons.ResourceNotFoundException;
import com.robiulsunyemon.transaction_service.transaction.mapper.TransactionMapper;
import com.robiulsunyemon.transaction_service.transaction.repository.TransactionRepository;
import com.robiulsunyemon.transaction_service.transaction.service.AuditPublisherService;
import com.robiulsunyemon.transaction_service.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionMapper transactionMapper;
    private final WalletClient walletClient;
    private final AuditPublisherService auditPublisherService;


    @Value("${rabbitmq.messaging.exchange}")
    private String exchange;

    @Value("${rabbitmq.messaging.routing-key}")
    private String routingKey;

    @Override
    @Transactional
    public void createTransaction(Long userId, String roleStr, TransactionRequest request, HttpServletRequest httpServletRequest) {
        log.info("Initiating transaction for user id: {} with type: {}", userId, request.getTxType());
        String ipAddress = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : "UNKNOWN";
        String deviceInfo = httpServletRequest != null ? httpServletRequest.getHeader("User-Agent") : "UNKNOWN";
        try {
            Role senderRole = null;
            if (roleStr != null && !roleStr.isBlank()) {
                String cleanRole = roleStr.replace("ROLE_", "").toUpperCase().trim();
                senderRole = Role.valueOf(cleanRole);
            }

            Role receiverRole = null;
            try {
                String fetchedRole = walletClient.getReceiverRole(request.getReceiverUserId());
                if (fetchedRole != null) {
                    receiverRole = Role.valueOf(fetchedRole.replace("ROLE_", "").toUpperCase().trim());
                }
            } catch (Exception e) {
                log.error("Failed to fetch receiver role from wallet-service for userId: {}", request.getReceiverUserId(), e);
                throw new ResourceNotFoundException("Wallet verification service is currently unavailable.",HttpStatus.FAILED_DEPENDENCY);
            }

            validateTransactionRules(request.getTxType(), senderRole, receiverRole);


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
           // Audit: Transaction Initiation Success (PENDING State)
            Map<String, Object> auditDetails = Map.of(
                    "sender_id", savedTransaction.getSenderUserId(),
                    "receiver_id", savedTransaction.getReceiverUserId(),
                    "amount", savedTransaction.getAmount(),
                    "charge", savedTransaction.getCharge(),
                    "commission", savedTransaction.getCommission(),
                    "tx_type", savedTransaction.getTxType().name(),
                    "status", "PENDING"
            );


            TransactionEvent transactionEvent=TransactionEvent.builder()
                    .txId(savedTransaction.getTxId())
                    .amount(savedTransaction.getAmount())
                    .charge(savedTransaction.getCharge())
                    .commission(savedTransaction.getCommission())
                    .failureReason(savedTransaction.getFailureReason())
                    .ipAddress(ipAddress)
                    .receiverUserId(savedTransaction.getReceiverUserId())
                    .senderUserId(savedTransaction.getSenderUserId())
                    .txStatus(savedTransaction.getTxStatus())
                    .txType(savedTransaction.getTxType())
                    .build();


            auditPublisherService.publishAudit(
                    "TRANSACTION_INITIATE", String.valueOf(userId), savedTransaction.getTxId(),
                    null, auditDetails, "SUCCESS", ipAddress, deviceInfo, null
            );

            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, transactionEvent);
                log.info("Successfully published transaction message to RabbitMQ exchange: {}", exchange);
            } catch (Exception e) {
                log.error("Failed to send transaction message to RabbitMQ for TxId: {}", savedTransaction.getTxId(), e);
                throw new BadRequestException("Message queue dispatch failed. Transaction rolled back.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // Audit: Transaction Initiation Failed
            Map<String, Object> failedDetails = Map.of(
                    "receiver_id", request.getReceiverUserId(),
                    "amount", request.getAmount(),
                    "tx_type", request.getTxType().name()
            );
            auditPublisherService.publishAudit(
                    "TRANSACTION_INITIATE", String.valueOf(userId), null,
                    null, failedDetails, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
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
    public void updateTransactionStatus(TransactionEvent event) {
        log.info("Updating final transaction status in database for TxId: {}", event.getTxId());

        TransactionEntity transaction = transactionRepository.findByTxId(event.getTxId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for TxId: " + event.getTxId(), HttpStatus.NOT_FOUND));

        Map<String, Object> oldStatusMap = Map.of("tx_status", transaction.getTxStatus().name());

        try {

            TransactionStatus finalStatus = TransactionStatus.valueOf(event.getTxStatus().name());
            transaction.setTxStatus(finalStatus);

            Map<String, Object> newStatusMap = Map.of(
                    "tx_status", finalStatus.name(),
                    "amount", transaction.getAmount()
            );

            if (finalStatus == TransactionStatus.FAILED && event.getFailureReason() != null) {
                log.warn("Transaction {} failed due to: {}", event.getTxId(), event.getFailureReason());

                try {
                    transaction.setFailureReason(event.getFailureReason());
                } catch (IllegalArgumentException e) {
                    log.error("Unknown failure reason received: {}. Falling back to SYSTEM_ERROR", event.getFailureReason());
                    transaction.setFailureReason(FailureReasonType.SYSTEM_ERROR);
                }

                newStatusMap = Map.of(
                        "tx_status", finalStatus.name(),
                        "amount", transaction.getAmount(),
                        "failure_reason", transaction.getFailureReason().name()
                );
            }

            transactionRepository.save(transaction);
            log.info("Transaction TxId: {} has been finalized to status: {}", event.getTxId(), finalStatus);

            // Audit: Transaction Final Status (SUCCESS/FAILED)
            auditPublisherService.publishAudit(
                    "TRANSACTION_FINALIZE", "SYSTEM", transaction.getTxId(),
                    oldStatusMap, newStatusMap, finalStatus.name(), "QUEUE_EVENT", "Wallet_Service_Callback", null
            );

        } catch (IllegalArgumentException e) {
            log.error("Invalid status value received from wallet service: {}", event.getTxStatus());

            log.error("Failed to update transaction status for TxId: {}", event.getTxId(), e);

            auditPublisherService.publishAudit(
                    "TRANSACTION_FINALIZE", "SYSTEM", event.getTxId(),
                    oldStatusMap, null, "FAILED", "QUEUE_EVENT", "Wallet_Service_Callback", e.getMessage()
            );
            throw e;
        }
    }

    private String generateUniqueTxId() {
        return "TX" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void validateTransactionRules(TransactionType txType, Role senderRole, Role receiverRole) {
        if (senderRole == null || receiverRole == null) {
            throw new IllegalArgumentException("Sender or Receiver role could not be verified.");
        }

        switch (txType) {
            case SEND_MONEY:
                if (senderRole != Role.LOCAL_USER || receiverRole != Role.LOCAL_USER) {
                    throw new IllegalArgumentException("Send Money is only allowed between Users.");
                }
                break;

            case CASH_OUT:
                if (senderRole != Role.LOCAL_USER || receiverRole != Role.AGENT) {
                    throw new IllegalArgumentException("Cash Out is strictly allowed from User to Agent only.");
                }
                break;

            case CASH_IN:
                if (senderRole != Role.AGENT || receiverRole != Role.LOCAL_USER) {
                    throw new IllegalArgumentException("Cash In is strictly allowed from Agent to User only.");
                }
                break;

            case PAYMENT:
                if (senderRole != Role.LOCAL_USER || receiverRole != Role.MERCHANT) {
                    throw new IllegalArgumentException("Payment is only allowed from User to Merchant.");
                }
                break;

            case MOBILE_RECHARGE:
            case RECEIVE_REMITTANCE:
                break;

            default:
                throw new IllegalArgumentException("Unsupported transaction type.");
        }
    }

}
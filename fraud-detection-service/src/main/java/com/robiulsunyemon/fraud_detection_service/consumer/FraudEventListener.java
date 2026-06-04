package com.robiulsunyemon.fraud_detection_service.consumer;
import com.robiulsunyemon.fraud_detection_service.dto.FailureReasonType;
import com.robiulsunyemon.fraud_detection_service.dto.TransactionEvent;
import com.robiulsunyemon.fraud_detection_service.dto.TransactionStatus;
import com.robiulsunyemon.fraud_detection_service.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudEventListener {

    private final FraudDetectionService fraudDetectionService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.messaging.exchange}")
    private String exchange;

    @Value("${rabbitmq.messaging.wallet-routing-key}")
    private String walletRoutingKey;

    @Value("${rabbitmq.messaging.rollback-routing-key}")
    private String rollbackRoutingKey;

    @RabbitListener(queues = "${rabbitmq.messaging.fraud-queue}")
    public void processFraudCheck(TransactionEvent event) {
        log.info("Received fraud check request for TxId: {}", event.getTxId());

        boolean isSafe = fraudDetectionService.validateTransaction(event);

        if (isSafe) {
            log.info("[PASSED] Transaction {} is safe. Notifying Wallet Service.", event.getTxId());
            rabbitTemplate.convertAndSend(exchange, walletRoutingKey, event);
        } else {
            log.warn("[BLOCKED] Transaction {} is fraudulent! Sending rollback to transaction-service.", event.getTxId());
            event.setTxStatus(TransactionStatus.FAILED);
            event.setFailureReason(FailureReasonType.SUSPICIOUS_ACTIVITY);
            rabbitTemplate.convertAndSend(exchange, rollbackRoutingKey, event);
        }
    }
}
package com.robiulsunyemon.transaction_service.transaction.consumer;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionEvent;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionEntity;
import com.robiulsunyemon.transaction_service.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSagaConsumer {

    private final TransactionService transactionService;

    @RabbitListener(queues = "${rabbitmq.messaging.rollback-queue}")
    public void consumeWalletResponse(TransactionEvent event) {
        log.info("Received Saga callback from wallet-service for TxId: {} with Status: {}",
                event.getTxId(), event.getTxStatus());

        try {
            transactionService.updateTransactionStatus(event);
        } catch (Exception e) {
            log.error("Failed to update final status for TxId: {}", event.getTxId(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.messaging.fraud-rollback-queue}")
    public void consumeFraudResponse(TransactionEvent event) {
        log.info("Received Saga callback from fraud-service for TxId: {} with Status: {}",
                event.getTxId(), event.getTxStatus());
        try {
            transactionService.updateTransactionStatus(event);
        } catch (Exception e) {
            log.error("Failed to update final status for TxId: {}", event.getTxId(), e);
        }
    }
}
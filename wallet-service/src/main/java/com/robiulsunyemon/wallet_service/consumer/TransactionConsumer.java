package com.robiulsunyemon.wallet_service.consumer;
import com.robiulsunyemon.wallet_service.wallet.dto.TransactionEvent;
import com.robiulsunyemon.wallet_service.wallet.entity.FailureReasonType;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final WalletService walletService;

    @RabbitListener(queues = "${rabbitmq.transaction-wallet-queue}")
    public void consumeTransactionMessage(TransactionEvent event) {
        log.info("Received transaction message from Queue for TxId: {}", event.getTxId());
        try {
            walletService.processWalletTransaction(event);
        } catch (Exception e) {
            log.error("Error processing wallet transaction for TxId: {}", event.getTxId(), e);
            walletService.sendRollbackMessage(event, FailureReasonType.SYSTEM_TIMEOUT);
        }
    }
}
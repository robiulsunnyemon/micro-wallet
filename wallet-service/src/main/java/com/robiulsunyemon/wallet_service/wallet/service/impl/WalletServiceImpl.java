package com.robiulsunyemon.wallet_service.wallet.service.impl;
import com.robiulsunyemon.wallet_service.wallet.config.RabbitMQConfig;
import com.robiulsunyemon.wallet_service.wallet.dto.*;
import com.robiulsunyemon.wallet_service.wallet.entity.CurrencyType;
import com.robiulsunyemon.wallet_service.wallet.entity.FailureReasonType;
import com.robiulsunyemon.wallet_service.wallet.entity.TransactionStatus;
import com.robiulsunyemon.wallet_service.wallet.entity.WalletEntity;
import com.robiulsunyemon.wallet_service.wallet.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.wallet_service.wallet.mapper.WalletMapper;
import com.robiulsunyemon.wallet_service.wallet.repository.WalletRepository;
import com.robiulsunyemon.wallet_service.wallet.service.AuditPublisherService;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final AuditPublisherService auditPublisherService;


    @Value("${rabbitmq.messaging.transaction-exchange}")
    private String exchange;

    @Value("${rabbitmq.messaging.transaction-rollback-routing-key}")
    private String rollbackRoutingKey;

    @Override
    @RabbitListener(queues = "${rabbitmq.messaging.queue}")
    public void createWallet(UserCreatedMessage userCreatedMessage) {
        try {
            if(walletRepository.findByUserId(userCreatedMessage.getUserId()).isPresent()) {

                Optional<WalletEntity> entity=walletRepository.findByUserId(userCreatedMessage.getUserId());
                entity.ifPresent(walletEntity -> {
                    WalletCreatedMessage walletCreatedMessage=
                            new WalletCreatedMessage(
                                    userCreatedMessage.getUserId(),
                                    entity.get().getId(),
                                    userCreatedMessage.getEmail(),
                                    userCreatedMessage.getPhoneNumber()
                            );
                    rabbitTemplate.convertAndSend(
                            rabbitMQConfig.getEXCHANGE_NAME(),
                            rabbitMQConfig.getROUTING_KEY(),
                            walletCreatedMessage
                    );
                });
                return ;
            }

            WalletRequest wallet=new WalletRequest();
            wallet.setUserId(userCreatedMessage.getUserId());
            wallet.setBalance(BigDecimal.valueOf(1000.00));
            wallet.setCurrency(CurrencyType.BDT);
            WalletEntity entity = walletMapper.requestToEntity(wallet);
            WalletEntity response=walletRepository.save(entity);
            System.out.println("Wallet successfully created for User ID: " + userCreatedMessage.getUserId());

            // Audit: Wallet Creation Success
            Map<String, Object> auditNewValue = Map.of(
                    "userId", response.getUserId(),
                    "initialBalance", response.getBalance(),
                    "currency", response.getCurrency().name()
            );
            auditPublisherService.publishAudit(
                    "WALLET_CREATION", "SYSTEM", String.valueOf(response.getId()),
                    null, auditNewValue, "SUCCESS", "QUEUE_EVENT", "RabbitMQ_Listener", null
            );




            WalletCreatedMessage walletCreatedMessage=new WalletCreatedMessage(userCreatedMessage.getUserId(),response.getId(),userCreatedMessage.getEmail(),userCreatedMessage.getPhoneNumber());
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getEXCHANGE_NAME(),
                    rabbitMQConfig.getROUTING_KEY(),
                    walletCreatedMessage
            );
            System.out.println("successfully message send to profile service");

        } catch (Exception e) {
            System.out.println("Error occur from wallet service. No message received from auth service. because: "+e);

            // Audit: Wallet Creation Failed
            auditPublisherService.publishAudit(
                    "WALLET_CREATION", "SYSTEM", String.valueOf(userCreatedMessage.getUserId()),
                    null, Map.of("userId", userCreatedMessage.getUserId()), "FAILED", "QUEUE_EVENT", "RabbitMQ_Listener", e.getMessage()
            );
            RegistrationStatusMessage rollbackMessage=new RegistrationStatusMessage(false,userCreatedMessage.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEXCHANGE_NAME(),rabbitMQConfig.getROLLBACK_ROUTING_KEY(),rollbackMessage);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.messaging.rollback-queue-profile}")
    @Override
    public void handleProfileRegistrationStatusUpdate(RegistrationStatusMessage statusMessage) {
        try{
            if (!statusMessage.getIsSucceed()){
                Optional<WalletEntity> entity=walletRepository.findByUserId(statusMessage.getUserId());
                entity.ifPresent(walletRepository::delete);

                // Audit: Wallet Rollback/Deletion
                auditPublisherService.publishAudit(
                        "WALLET_ROLLBACK_DELETE", "SYSTEM", String.valueOf(entity.get().getId()),
                        Map.of("walletId", entity.get().getId(), "userId", statusMessage.getUserId(), "balance", entity.get().getBalance()),
                        null, "SUCCESS", "QUEUE_EVENT", "Profile_Rollback_Trigger", null
                );


                rabbitTemplate.convertAndSend(
                        rabbitMQConfig.getEXCHANGE_NAME(),
                        rabbitMQConfig.getROLLBACK_ROUTING_KEY(),
                        statusMessage
                );
            }else {
                rabbitTemplate.convertAndSend(
                        rabbitMQConfig.getEXCHANGE_NAME(),
                        rabbitMQConfig.getROLLBACK_ROUTING_KEY(),
                        statusMessage

                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public List<WalletResponse> fetchWallet() {
        return walletRepository.findAll().stream()
                .map(walletMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WalletResponse> findByWalletId(Long id) {
        return walletRepository.findById(id)
                .map(walletMapper::entityToResponse);
    }

    @Override
    public WalletResponse findWalletByUserId(Long userId) {
        WalletEntity entity=walletRepository.findByUserId(userId).orElseThrow(()->new ResourceNotFoundException("User does not found", HttpStatus.NOT_FOUND));
        return walletMapper.entityToResponse(entity);
    }

    @Override
    public String deleteWalletById(Long id) {
        if (walletRepository.existsById(id)) {
            walletRepository.deleteById(id);
            return "Wallet deleted successfully";
        }
        return "Wallet not found";
    }



    @Override
    @Transactional
    public void processWalletTransaction(TransactionEvent event) {
       try {
           WalletEntity senderWallet = walletRepository.findByUserId(event.getSenderUserId())
                   .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
           BigDecimal totalDeductAmount = event.getAmount().add(event.getCharge());

           BigDecimal senderOldBalance=senderWallet.getBalance();

           if (senderWallet.getBalance().compareTo(totalDeductAmount) < 0) {
               log.warn("Insufficient balance for User: {}. Required: {}, Available: {}",
                       event.getSenderUserId(), totalDeductAmount, senderWallet.getBalance());

               // Audit: Transaction Failed due to Insufficient Balance
               auditPublisherService.publishAudit(
                       "WALLET_TRANSACTION", String.valueOf(event.getSenderUserId()), event.getTxId(),
                       Map.of("balance", senderWallet.getBalance()), null, "FAILED",
                       "INTERNAL", "Transaction_Service_Event", "Insufficient balance"
               );

               sendRollbackMessage(event, FailureReasonType.INSUFFICIENT_BALANCE);
               return;
           }

           senderWallet.setBalance(senderWallet.getBalance().subtract(totalDeductAmount));
           walletRepository.save(senderWallet);


           WalletEntity receiverWallet = walletRepository.findByUserId(event.getReceiverUserId())
                   .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));


           BigDecimal totalCreditAmount = event.getAmount().add(event.getCommission());
           receiverWallet.setBalance(receiverWallet.getBalance().add(totalCreditAmount));
           walletRepository.save(receiverWallet);

           log.info("Wallet balances updated successfully for TxId: {}", event.getTxId());


           receiverWallet.setBalance(receiverWallet.getBalance().add(totalCreditAmount));
           WalletEntity savedReceiver = walletRepository.save(receiverWallet);

           // Audit: Successful Transaction
           Map<String, Object> auditOldBalances = Map.of(
                   "sender_old_balance", senderOldBalance,
                   "receiver_old_balance",  savedReceiver.getBalance().subtract(event.getAmount())
           );
           Map<String, Object> auditNewBalances = Map.of(
                   "tx_amount", event.getAmount(),
                   "sender_new_balance", senderWallet.getBalance(),
                   "receiver_new_balance", savedReceiver.getBalance()
           );
           auditPublisherService.publishAudit(
                   "WALLET_TRANSACTION", String.valueOf(event.getSenderUserId()), event.getTxId(),
                   auditOldBalances, auditNewBalances, "SUCCESS", "INTERNAL", "Transaction_Service_Event", null
           );

           event.setTxStatus(TransactionStatus.SUCCESS);
           rabbitTemplate.convertAndSend(exchange, rollbackRoutingKey, event);
       } catch (Exception e) {
           // Audit: System Error during transaction
           auditPublisherService.publishAudit(
                   "WALLET_TRANSACTION", String.valueOf(event.getSenderUserId()), event.getTxId(),
                   null, null, "FAILED", "INTERNAL", "Transaction_Service_Event", e.getMessage()
           );

           sendRollbackMessage(event, FailureReasonType.SYSTEM_ERROR);
           throw new RuntimeException(e);
       }

    }


    @Override
    public void sendRollbackMessage(TransactionEvent event, FailureReasonType reason) {
        log.info("Sending rollback/failure message to Transaction Service for TxId: {}", event.getTxId());
        event.setTxStatus(TransactionStatus.FAILED);
        event.setFailureReason(reason);
        rabbitTemplate.convertAndSend(exchange, rollbackRoutingKey, event);
    }
}

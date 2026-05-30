package com.robiulsunyemon.wallet_service.wallet.service.impl;
import com.robiulsunyemon.wallet_service.wallet.config.RabbitMQConfig;
import com.robiulsunyemon.wallet_service.wallet.dto.*;
import com.robiulsunyemon.wallet_service.wallet.entity.WalletEntity;
import com.robiulsunyemon.wallet_service.wallet.mapper.WalletMapper;
import com.robiulsunyemon.wallet_service.wallet.repository.WalletRepository;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService {

    private WalletRepository walletRepository;
    private WalletMapper walletMapper;
    private RabbitTemplate rabbitTemplate;
    private RabbitMQConfig rabbitMQConfig;

    @Override
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void createWallet(UserCreatedMessage userCreatedMessage) {
        try {
            if(walletRepository.findByUserId(userCreatedMessage.getUserId()).isPresent()) {

                Optional<WalletEntity> entity=walletRepository.findByUserId(userCreatedMessage.getUserId());
                entity.ifPresent(walletEntity -> {
                    WalletCreatedMessage walletCreatedMessage=new WalletCreatedMessage(userCreatedMessage.getUserId(),entity.get().getId(),userCreatedMessage.getEmail(),userCreatedMessage.getPhoneNumber());
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
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCurrency("BDT");
            WalletEntity entity = walletMapper.requestToEntity(wallet);
            WalletEntity response=walletRepository.save(entity);
            System.out.println("Wallet successfully created for User ID: " + userCreatedMessage.getUserId());
            WalletCreatedMessage walletCreatedMessage=new WalletCreatedMessage(userCreatedMessage.getUserId(),response.getId(),userCreatedMessage.getEmail(),userCreatedMessage.getPhoneNumber());
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getEXCHANGE_NAME(),
                    rabbitMQConfig.getROUTING_KEY(),
                    walletCreatedMessage
            );
            System.out.println("successfully message send to profile service");

        } catch (Exception e) {
            System.out.println("Error occur from wallet service. No message received from auth service. because: "+e);
            RegistrationStatusMessage rollbackMessage=new RegistrationStatusMessage(false,userCreatedMessage.getUserId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEXCHANGE_NAME(),rabbitMQConfig.getROLLBACK_ROUTING_KEY(),rollbackMessage);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.rollback-queue-profile}")
    @Override
    public void handleProfileRegistrationStatusUpdate(RegistrationStatusMessage statusMessage) {
        try{
            if (!statusMessage.getIsSucceed()){
                Optional<WalletEntity> entity=walletRepository.findByUserId(statusMessage.getUserId());
                entity.ifPresent(walletEntity -> walletRepository.delete(walletEntity));
                rabbitTemplate.convertAndSend(rabbitMQConfig.getEXCHANGE_NAME(),rabbitMQConfig.getROLLBACK_ROUTING_KEY(),statusMessage);
            }else {
                rabbitTemplate.convertAndSend(rabbitMQConfig.getEXCHANGE_NAME(),rabbitMQConfig.getROLLBACK_ROUTING_KEY(),statusMessage);
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
    public String deleteWalletById(Long id) {
        if (walletRepository.existsById(id)) {
            walletRepository.deleteById(id);
            return "Wallet deleted successfully";
        }
        return "Wallet not found";
    }
}

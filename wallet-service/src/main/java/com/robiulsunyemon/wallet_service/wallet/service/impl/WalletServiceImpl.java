package com.robiulsunyemon.wallet_service.wallet.service.impl;
import com.robiulsunyemon.wallet_service.wallet.dto.UserCreatedMessage;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import com.robiulsunyemon.wallet_service.wallet.entity.WalletEntity;
import com.robiulsunyemon.wallet_service.wallet.mapper.WalletMapper;
import com.robiulsunyemon.wallet_service.wallet.repository.WalletRepository;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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


    @Override
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void createWallet(UserCreatedMessage userCreatedMessage) {

        if(walletRepository.findByUserId(userCreatedMessage.getUserId()).isPresent()) {
            return ;
        }

        WalletRequest wallet=new WalletRequest();
        wallet.setUserId(userCreatedMessage.getUserId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("BDT");
        WalletEntity entity = walletMapper.requestToEntity(wallet);
        WalletEntity savedEntity = walletRepository.save(entity);
        System.out.println("Wallet successfully created for User ID: " + userCreatedMessage.getUserId());
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

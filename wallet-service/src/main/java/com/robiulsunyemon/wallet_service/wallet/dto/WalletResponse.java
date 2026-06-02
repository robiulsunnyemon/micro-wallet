package com.robiulsunyemon.wallet_service.wallet.dto;
import com.robiulsunyemon.wallet_service.wallet.entity.CurrencyType;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Data
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private CurrencyType currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

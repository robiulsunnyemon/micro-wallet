package com.robiulsunyemon.wallet_service.wallet.dto;
import com.robiulsunyemon.wallet_service.wallet.entity.CurrencyType;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Data
public class WalletRequest {
    private Long userId;
    private BigDecimal balance;
    private CurrencyType currency;
}

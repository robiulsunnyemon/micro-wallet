package com.robiulsunyemon.transaction_service.transaction.dto;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;


@Data
@Builder
public class TransactionRequest {
    private String parentTxId;
    private Long receiverUserId;
    private BigDecimal amount;;
    private TransactionType txType;
    private String reference;
}

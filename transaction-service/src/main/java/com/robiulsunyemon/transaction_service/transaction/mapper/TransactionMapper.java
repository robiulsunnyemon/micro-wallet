package com.robiulsunyemon.transaction_service.transaction.mapper;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {


    public TransactionResponse entityToResponse(TransactionEntity entity){
        TransactionResponse response = new TransactionResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setTxId(entity.getTxId());
        response.setParentTxId(entity.getParentTxId());
        response.setSenderUserId(entity.getSenderUserId());
        response.setReceiverUserId(entity.getReceiverUserId());
        response.setAmount(entity.getAmount());
        response.setCharge(entity.getCharge());
        response.setCommission(entity.getCommission());
        response.setCurrency(entity.getCurrency());
        response.setTxType(entity.getTxType());
        response.setTxStatus(entity.getTxStatus());
        response.setFailureReason(entity.getFailureReason());
        response.setReference(entity.getReference());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

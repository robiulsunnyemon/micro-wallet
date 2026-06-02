package com.robiulsunyemon.transaction_service.transaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "AUTH-SERVICE")
public interface WalletClient {
    @GetMapping("/internal/role/{userId}")
    String getReceiverRole(@PathVariable("userId") Long userId);
}
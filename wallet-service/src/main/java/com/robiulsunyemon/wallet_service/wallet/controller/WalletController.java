package com.robiulsunyemon.wallet_service.wallet.controller;

import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public List<WalletResponse> fetchAllWallets(){
        return walletService.fetchWallet();
    }

    @GetMapping("/{id}")
    public Optional<WalletResponse> getWalletById(@PathVariable Long id){
        return walletService.findByWalletId(id);
    }

    @DeleteMapping("/{id}")
    public String deleteWallet(@PathVariable Long id){
        return walletService.deleteWalletById(id);
    }
}

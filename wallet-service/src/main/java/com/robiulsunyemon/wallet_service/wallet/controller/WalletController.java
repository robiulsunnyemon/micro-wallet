package com.robiulsunyemon.wallet_service.wallet.controller;

import com.robiulsunyemon.wallet_service.wallet.dto.GlobalResponse;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletRequest;
import com.robiulsunyemon.wallet_service.wallet.dto.WalletResponse;
import com.robiulsunyemon.wallet_service.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<GlobalResponse<List<WalletResponse>>> fetchAllWallets(HttpServletRequest request){
        List<WalletResponse> responses=walletService.fetchWallet();
        return buildSuccessResponse(
                responses,
                HttpStatus.OK,
                "Success",
                request.getRequestURI()
        );
    }


    @GetMapping("/me")
    public ResponseEntity<GlobalResponse<WalletResponse>> fetchWalletByUserId(
            @RequestHeader(value = "userId", required = false) Long userId,
            HttpServletRequest request) {
        if (userId == null) {
            return buildSuccessResponse(null, HttpStatus.UNAUTHORIZED,
                    "Missing userId header", request.getRequestURI());
        }
        WalletResponse response = walletService.findWalletByUserId(userId);
        return buildSuccessResponse(response, HttpStatus.OK,
                "Success", request.getRequestURI());
    }

    @DeleteMapping("/{id}")
    public String deleteWallet(@PathVariable Long id){
        return walletService.deleteWalletById(id);
    }


    private <T> ResponseEntity<GlobalResponse<T>> buildSuccessResponse(T data, HttpStatus status, String message, String path) {
        GlobalResponse<T> response = GlobalResponse.<T>builder()
                .statusCode(status.value())
                .success(true)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
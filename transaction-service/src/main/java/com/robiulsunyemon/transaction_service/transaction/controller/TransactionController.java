package com.robiulsunyemon.transaction_service.transaction.controller;

import com.robiulsunyemon.transaction_service.transaction.dto.GlobalResponse;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionRequest;
import com.robiulsunyemon.transaction_service.transaction.dto.TransactionResponse;
import com.robiulsunyemon.transaction_service.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<GlobalResponse<List<TransactionResponse>>> fetchAllTransactions(HttpServletRequest request) {
        List<TransactionResponse> responses = transactionService.fetchTransaction();
        return buildSuccessResponse(
                responses,
                HttpStatus.OK,
                "Transactions fetched successfully",
                request.getRequestURI()
        );
    }

    @PostMapping
    public ResponseEntity<GlobalResponse<String>> createTransaction(
            @RequestHeader(value = "userId") Long userId,
            @RequestHeader(value = "role") String role,
            HttpServletRequest request,
            @RequestBody TransactionRequest transactionRequest,
            HttpServletRequest servletRequest) {

            if (userId == null) {
                return buildSuccessResponse(null, HttpStatus.UNAUTHORIZED,
                        "Missing userId header", request.getRequestURI());
            }
        transactionService.createTransaction(userId, role, transactionRequest,request);

        return buildSuccessResponse(
                "Processing your payment securely. Please do not close the app or refresh the page.",
                HttpStatus.ACCEPTED,
                "Transaction initiated successfully",
                servletRequest.getRequestURI()
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<String>> deleteTransaction(
            @PathVariable Long id,
            HttpServletRequest request) {

        String result = transactionService.deleteTransactionById(id);

        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "Transaction deletion processed",
                request.getRequestURI()
        );
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
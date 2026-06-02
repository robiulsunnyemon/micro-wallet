package com.robiulsunyemon.transaction_service.transaction.entity;

public enum FailureReasonType {
    NONE,
    INSUFFICIENT_BALANCE,
    INVALID_WALLET_NUMBER,
    DAILY_LIMIT_EXCEEDED,
    MONTHLY_LIMIT_EXCEEDED,
    PIN_VERIFICATION_FAILED,
    SYSTEM_TIMEOUT,
    SYSTEM_ERROR
}
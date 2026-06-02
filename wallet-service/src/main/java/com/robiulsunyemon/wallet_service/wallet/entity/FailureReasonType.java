package com.robiulsunyemon.wallet_service.wallet.entity;

public enum FailureReasonType {
    NONE,
    INSUFFICIENT_BALANCE,
    INVALID_WALLET_NUMBER,
    DAILY_LIMIT_EXCEEDED,
    MONTHLY_LIMIT_EXCEEDED,
    PIN_VERIFICATION_FAILED,
    SYSTEM_TIMEOUT
}
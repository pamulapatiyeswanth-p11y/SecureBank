package com.securebank.accountservice.exception;

public class MinimumBalanceNotMetException extends RuntimeException {
    public MinimumBalanceNotMetException(String message) {
        super(message);
    }
}

package com.learning.banking.application.exception;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException(String message) {
        super(message);
    }
}

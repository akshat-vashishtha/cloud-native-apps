package com.learning.banking.domain.exception;

public class ConcurrentTransactionException extends RuntimeException {

    public ConcurrentTransactionException(String message) {
        super(message);
    }

}

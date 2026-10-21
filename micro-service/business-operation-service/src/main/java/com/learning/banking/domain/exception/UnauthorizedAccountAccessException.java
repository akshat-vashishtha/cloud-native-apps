package com.learning.banking.domain.exception;

public class UnauthorizedAccountAccessException extends RuntimeException {

    public UnauthorizedAccountAccessException(String message) {
        super(message);
    }

}

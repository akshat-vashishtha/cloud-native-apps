package com.learning.banking.application.service;

import com.learning.banking.application.dto.AccountResponse;
import com.learning.banking.application.dto.CreateAccountRequest;
import com.learning.banking.application.dto.TransferFundsRequest;
import com.learning.banking.application.dto.TransferResponse;
import com.learning.banking.domain.enums.CardStatus;

public interface BankingService {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse getAccountById(String accountId);
    AccountResponse getAccountByAccountNumber(String accountNumber);
    TransferResponse transferFunds(TransferFundsRequest request, String authenticatedUserId, String idempotencyKey);
    void updateCardStatus(String cardId, String userId, CardStatus status);
}

package com.learning.banking.presentation;

import com.learning.banking.application.dto.AccountResponse;
import com.learning.banking.application.dto.CreateAccountRequest;
import com.learning.banking.application.dto.TransferFundsRequest;
import com.learning.banking.application.dto.TransferResponse;
import com.learning.banking.application.service.BankingService;
import com.learning.banking.domain.enums.CardStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/banking")
public class BankingController {

    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @PostMapping("/accounts")
    @PreAuthorize("hasAuthority('account:read')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankingService.createAccount(request));
    }

    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAuthority('account:read')")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable String accountId) {
        return ResponseEntity.ok(bankingService.getAccountById(accountId));
    }

    @GetMapping("/accounts/number/{accountNumber}")
    @PreAuthorize("hasAuthority('account:read')")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankingService.getAccountByAccountNumber(accountNumber));
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority('transfer:execute')")
    public ResponseEntity<TransferResponse> transferFunds(
            @Valid @RequestBody TransferFundsRequest request,
            @RequestAttribute(value = "authenticatedUserId", required = false) String authenticatedUserId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(bankingService.transferFunds(request, authenticatedUserId, idempotencyKey));
    }

    @PatchMapping("/cards/{cardId}/status")
    @PreAuthorize("hasAuthority('card:update_status')")
    public ResponseEntity<Void> updateCardStatus(
            @PathVariable String cardId,
            @RequestAttribute(value = "authenticatedUserId", required = false) String authenticatedUserId,
            @RequestParam CardStatus status) {
        bankingService.updateCardStatus(cardId, authenticatedUserId, status);
        return ResponseEntity.noContent().build();
    }
}

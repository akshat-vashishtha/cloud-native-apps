package com.learning.ai.infrastructure.grpc;

import com.learning.ai.infrastructure.config.AiServiceProperties;
import com.learning.banking.grpc.v1.BankingGrpcServiceGrpc;
import com.learning.banking.grpc.v1.GetBalanceRequest;
import com.learning.banking.grpc.v1.GetBalanceResponse;
import com.learning.banking.grpc.v1.TransferFundsRequest;
import com.learning.banking.grpc.v1.TransferFundsResponse;
import com.learning.banking.grpc.v1.UpdateCardRequest;
import com.learning.banking.grpc.v1.UpdateCardResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcBankingClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcBankingClient.class);

    @GrpcClient("banking-service")
    private BankingGrpcServiceGrpc.BankingGrpcServiceBlockingStub bankingStub;

    private final AiServiceProperties properties;

    public GrpcBankingClient(AiServiceProperties properties) {
        this.properties = properties;
    }

    public AccountBalanceResult getAccountBalance(String accountId, String userId) {
        log.info("gRPC Client -> BankingGrpcService.getAccountBalance(accountId={}, userId={})", accountId, userId);
        GetBalanceResponse response = bankingStub
                .withDeadlineAfter(properties.grpcDeadlineMs(), TimeUnit.MILLISECONDS)
                .getAccountBalance(GetBalanceRequest.newBuilder()
                        .setAccountId(accountId)
                        .setUserId(userId)
                        .build());

        return new AccountBalanceResult(
                response.getAccountId(),
                response.getBalance(),
                response.getCurrency(),
                response.getStatus()
        );
    }

    public TransferExecutionResult executeTransfer(
            String sourceAccountId,
            String targetAccountId,
            BigDecimal amount,
            String currency,
            String userId,
            String description
    ) {
        log.info("gRPC Client -> BankingGrpcService.transferFunds(src={}, dst={}, amount={} {})",
                sourceAccountId, targetAccountId, amount, currency);
        TransferFundsResponse response = bankingStub
                .withDeadlineAfter(properties.grpcDeadlineMs() * 2, TimeUnit.MILLISECONDS)
                .transferFunds(TransferFundsRequest.newBuilder()
                        .setSourceAccountId(sourceAccountId)
                        .setTargetAccountId(targetAccountId)
                        .setAmount(amount.doubleValue())
                        .setCurrency(currency)
                        .setUserId(userId)
                        .setDescription(description != null ? description : "AI Assisted Transfer")
                        .build());

        return new TransferExecutionResult(
                response.getTransactionId(),
                response.getStatus(),
                response.getMessage()
        );
    }

    public CardStatusResult updateCardStatus(String cardId, String status, String userId) {
        log.info("gRPC Client -> BankingGrpcService.updateCardStatus(cardId={}, status={})", cardId, status);
        UpdateCardResponse response = bankingStub
                .withDeadlineAfter(properties.grpcDeadlineMs(), TimeUnit.MILLISECONDS)
                .updateCardStatus(UpdateCardRequest.newBuilder()
                        .setCardId(cardId)
                        .setStatus(status)
                        .setUserId(userId)
                        .build());

        return new CardStatusResult(response.getCardId(), response.getStatus());
    }

    public record AccountBalanceResult(String accountId, double balance, String currency, String status) {}

    public record TransferExecutionResult(String transactionId, String status, String message) {}

    public record CardStatusResult(String cardId, String status) {}
}

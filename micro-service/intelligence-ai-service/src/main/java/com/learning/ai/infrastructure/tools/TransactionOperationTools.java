package com.learning.ai.infrastructure.tools;

import com.learning.ai.domain.model.PendingConfirmation;
import com.learning.ai.infrastructure.cache.RedisConfirmationTokenService;
import com.learning.ai.infrastructure.config.AiServiceProperties;
import com.learning.ai.infrastructure.grpc.GrpcBankingClient;
import com.learning.ai.infrastructure.grpc.GrpcUserRbacClient;
import com.learning.ai.infrastructure.messaging.KafkaAiStreamPublisher;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class TransactionOperationTools {

    private final GrpcBankingClient bankingClient;
    private final GrpcUserRbacClient userRbacClient;
    private final RedisConfirmationTokenService confirmationTokenService;
    private final KafkaAiStreamPublisher streamPublisher;
    private final AiServiceProperties properties;

    public TransactionOperationTools(
            GrpcBankingClient bankingClient,
            GrpcUserRbacClient userRbacClient,
            RedisConfirmationTokenService confirmationTokenService,
            KafkaAiStreamPublisher streamPublisher,
            AiServiceProperties properties
    ) {
        this.bankingClient = bankingClient;
        this.userRbacClient = userRbacClient;
        this.confirmationTokenService = confirmationTokenService;
        this.streamPublisher = streamPublisher;
        this.properties = properties;
    }

    @Tool(description = "Check real-time bank account balance and status via gRPC for a given account ID")
    public GrpcBankingClient.AccountBalanceResult checkAccountBalance(
            @ToolParam(description = "The account ID to query") String accountId,
            @ToolParam(description = "The authenticated user ID") String userId
    ) {
        return bankingClient.getAccountBalance(accountId, userId);
    }

    @Tool(description = "Stage a 2-step human-in-the-loop (HITL) verified fund transfer. Returns a confirmationToken sent to the user's STOMP confirmation stream.")
    public String requestFundTransferConfirmation(
            @ToolParam(description = "Source account ID") String sourceAccountId,
            @ToolParam(description = "Destination account ID") String targetAccountId,
            @ToolParam(description = "Amount to transfer") BigDecimal amount,
            @ToolParam(description = "Currency code e.g. USD") String currency,
            @ToolParam(description = "Authenticated user ID") String userId,
            @ToolParam(description = "Reason or description for the transfer") String description
    ) {
        if (!userRbacClient.hasPermission(userId, "transfer:execute")) {
            return "PERMISSION_DENIED: User lacks 'transfer:execute' permission to stage this fund transfer.";
        }

        String token = "cfm_" + UUID.randomUUID();
        String summary = String.format(
                "Confirm transfer of %s %s from %s to %s (%s)",
                amount, currency, sourceAccountId, targetAccountId, description
        );
        PendingConfirmation pending = new PendingConfirmation(
                token,
                userId,
                "TRANSFER_FUNDS",
                sourceAccountId,
                targetAccountId,
                amount,
                currency,
                description,
                null,
                null,
                summary,
                Instant.now().plus(Duration.ofMinutes(properties.confirmationTtlMinutes()))
        );
        confirmationTokenService.savePendingConfirmation(pending);
        streamPublisher.publishConfirmationPrompt(pending);
        return "HITL_CONFIRMATION_REQUIRED: token=" + token + " | " + summary;
    }
}

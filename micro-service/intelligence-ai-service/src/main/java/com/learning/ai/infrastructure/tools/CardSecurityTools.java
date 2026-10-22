package com.learning.ai.infrastructure.tools;

import com.learning.ai.domain.model.PendingConfirmation;
import com.learning.ai.infrastructure.cache.RedisConfirmationTokenService;
import com.learning.ai.infrastructure.config.AiServiceProperties;
import com.learning.ai.infrastructure.grpc.GrpcUserRbacClient;
import com.learning.ai.infrastructure.messaging.KafkaAiStreamPublisher;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class CardSecurityTools {

    private final GrpcUserRbacClient userRbacClient;
    private final RedisConfirmationTokenService confirmationTokenService;
    private final KafkaAiStreamPublisher streamPublisher;
    private final AiServiceProperties properties;

    public CardSecurityTools(
            GrpcUserRbacClient userRbacClient,
            RedisConfirmationTokenService confirmationTokenService,
            KafkaAiStreamPublisher streamPublisher,
            AiServiceProperties properties
    ) {
        this.userRbacClient = userRbacClient;
        this.confirmationTokenService = confirmationTokenService;
        this.streamPublisher = streamPublisher;
        this.properties = properties;
    }

    @Tool(description = "Stage a 2-step human-in-the-loop (HITL) confirmation to lock or unlock a payment card.")
    public String requestCardStatusUpdateConfirmation(
            @ToolParam(description = "Card ID to lock or unlock") String cardId,
            @ToolParam(description = "Desired card status: LOCKED or ACTIVE") String status,
            @ToolParam(description = "Authenticated user ID") String userId
    ) {
        if (!userRbacClient.hasPermission(userId, "card:update_status")) {
            return "PERMISSION_DENIED: User lacks 'card:update_status' permission to modify card status.";
        }

        String token = "cfm_" + UUID.randomUUID();
        String summary = String.format("Confirm setting card %s status to %s", cardId, status);
        PendingConfirmation pending = new PendingConfirmation(
                token,
                userId,
                "UPDATE_CARD_STATUS",
                null,
                null,
                null,
                null,
                null,
                cardId,
                status,
                summary,
                Instant.now().plus(Duration.ofMinutes(properties.confirmationTtlMinutes()))
        );
        confirmationTokenService.savePendingConfirmation(pending);
        streamPublisher.publishConfirmationPrompt(pending);
        return "HITL_CONFIRMATION_REQUIRED: token=" + token + " | " + summary;
    }
}

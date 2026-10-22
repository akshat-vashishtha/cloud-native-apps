package com.learning.ai.application.service;

import com.learning.ai.application.agent.IntentRouterAgent;
import com.learning.ai.application.agent.RiskEvaluatorAgent;
import com.learning.ai.application.agent.SpecialistAiAgent;
import com.learning.ai.application.dto.AiChatRequestDto;
import com.learning.ai.application.dto.AiChatResponseDto;
import com.learning.ai.application.dto.ChatTurnResponseDto;
import com.learning.ai.application.dto.ConfirmActionResponseDto;
import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentExecutionContext;
import com.learning.ai.domain.model.ChatTurn;
import com.learning.ai.domain.model.PendingConfirmation;
import com.learning.ai.infrastructure.cache.RedisConfirmationTokenService;
import com.learning.ai.infrastructure.cache.RedisSemanticCacheService;
import com.learning.ai.infrastructure.config.AiServiceProperties;
import com.learning.ai.infrastructure.grpc.GrpcBankingClient;
import com.learning.ai.infrastructure.messaging.KafkaAiStreamPublisher;
import com.learning.ai.infrastructure.persistence.criteria.ChatMemoryCriteriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiConciergeServiceImpl implements AiConciergeService {

    private static final Logger log = LoggerFactory.getLogger(AiConciergeServiceImpl.class);

    private final IntentRouterAgent intentRouterAgent;
    private final Map<AgentCategory, SpecialistAiAgent> specialistAgentsByCategory;
    private final RiskEvaluatorAgent riskEvaluatorAgent;
    private final GrpcBankingClient bankingClient;
    private final RedisSemanticCacheService semanticCacheService;
    private final RedisConfirmationTokenService confirmationTokenService;
    private final ChatMemoryCriteriaRepository chatMemoryRepository;
    private final KafkaAiStreamPublisher streamPublisher;
    private final AiServiceProperties properties;

    public AiConciergeServiceImpl(
            IntentRouterAgent intentRouterAgent,
            List<SpecialistAiAgent> registeredSpecialistAgents,
            RiskEvaluatorAgent riskEvaluatorAgent,
            GrpcBankingClient bankingClient,
            RedisSemanticCacheService semanticCacheService,
            RedisConfirmationTokenService confirmationTokenService,
            ChatMemoryCriteriaRepository chatMemoryRepository,
            KafkaAiStreamPublisher streamPublisher,
            AiServiceProperties properties
    ) {
        this.intentRouterAgent = intentRouterAgent;
        this.specialistAgentsByCategory = new EnumMap<>(AgentCategory.class);
        for (SpecialistAiAgent specialistAgent : registeredSpecialistAgents) {
            this.specialistAgentsByCategory.put(specialistAgent.supportedCategory(), specialistAgent);
        }
        this.riskEvaluatorAgent = riskEvaluatorAgent;
        this.bankingClient = bankingClient;
        this.semanticCacheService = semanticCacheService;
        this.confirmationTokenService = confirmationTokenService;
        this.chatMemoryRepository = chatMemoryRepository;
        this.streamPublisher = streamPublisher;
        this.properties = properties;
    }

    @Override
    public AiChatResponseDto converse(String userId, AiChatRequestDto request) {
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : "default-" + userId;
        String conversationId = userId + ":" + sessionId;
        String messageId = UUID.randomUUID().toString();

        // 1. Agentic Routing Workflow: IntentRouterAgent classifies prompt
        AgentCategory routedCategory = intentRouterAgent.classifyIntent(request.prompt());

        // 2. Semantic Cache Pre-Check for FAQ inquiries (OWASP LLM10 Cost Control)
        if (routedCategory == AgentCategory.CONCIERGE_FAQ) {
            Optional<String> cachedCompletion = semanticCacheService.lookupCachedCompletion(request.prompt());
            if (cachedCompletion.isPresent()) {
                log.info("Semantic cache hit for user [{}] on FAQ prompt", userId);
                String cachedReply = cachedCompletion.get();
                streamPublisher.publishStreamChunk(userId, sessionId, messageId, cachedReply, true);
                return new AiChatResponseDto(messageId, sessionId, routedCategory, cachedReply, true, Instant.now());
            }
        }

        // 3. Load Short-Term Working Memory from MongoDB via Criteria API
        List<ChatTurn> recentConversationTurns = chatMemoryRepository.findRecentTurns(
                userId,
                sessionId,
                properties.shortTermMemoryWindow()
        );
        String formattedMemoryContext = recentConversationTurns.stream()
                .map(chatTurn -> chatTurn.role() + ": " + chatTurn.content())
                .collect(Collectors.joining("\n"));

        // 4. Persist User Turn in MongoDB
        chatMemoryRepository.appendTurn(new ChatTurn(
                UUID.randomUUID().toString(),
                userId,
                sessionId,
                "USER",
                request.prompt(),
                Instant.now()
        ));

        // 5. Dispatch to Specialist Agent (Strategy Pattern + Least-Privilege Tools)
        SpecialistAiAgent specialistAgent = specialistAgentsByCategory.get(routedCategory);
        if (specialistAgent == null) {
            throw new IllegalStateException("No specialist agent registered for category: " + routedCategory);
        }

        AgentExecutionContext executionContext = new AgentExecutionContext(
                userId,
                sessionId,
                conversationId,
                request.prompt(),
                formattedMemoryContext.isBlank() ? "(none)" : formattedMemoryContext,
                routedCategory
        );

        String candidateReply = specialistAgent.execute(executionContext);

        // 6. Evaluator-Optimizer Workflow: RiskEvaluatorAgent verifies output safety
        String verifiedReply = riskEvaluatorAgent.verifyAndRefineResponse(request.prompt(), candidateReply);

        // 7. Persist Assistant Turn & Update Semantic Cache
        chatMemoryRepository.appendTurn(new ChatTurn(
                messageId,
                userId,
                sessionId,
                "ASSISTANT",
                verifiedReply,
                Instant.now()
        ));

        if (routedCategory == AgentCategory.CONCIERGE_FAQ) {
            semanticCacheService.storeCompletion(request.prompt(), verifiedReply);
        }

        // 8. Stream tokens to STOMP /user/{userId}/queue/ai-stream via Kafka relay
        streamReplyTokens(userId, sessionId, messageId, verifiedReply);

        return new AiChatResponseDto(messageId, sessionId, routedCategory, verifiedReply, false, Instant.now());
    }

    @Override
    public ConfirmActionResponseDto confirmPendingAction(String userId, String confirmationToken) {
        PendingConfirmation pendingConfirmation = confirmationTokenService.consumePendingConfirmation(confirmationToken, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid, expired, or unauthorized confirmation token"));

        if ("TRANSFER_FUNDS".equals(pendingConfirmation.actionType())) {
            GrpcBankingClient.TransferExecutionResult transferResult = bankingClient.executeTransfer(
                    pendingConfirmation.sourceAccountId(),
                    pendingConfirmation.targetAccountId(),
                    pendingConfirmation.amount(),
                    pendingConfirmation.currency(),
                    userId,
                    pendingConfirmation.description()
            );
            return new ConfirmActionResponseDto(
                    confirmationToken,
                    pendingConfirmation.actionType(),
                    transferResult.status(),
                    "Executed transfer TX=" + transferResult.transactionId() + ": " + transferResult.message(),
                    Instant.now()
            );
        }

        if ("UPDATE_CARD_STATUS".equals(pendingConfirmation.actionType())) {
            GrpcBankingClient.CardStatusResult cardStatusResult = bankingClient.updateCardStatus(
                    pendingConfirmation.cardId(),
                    pendingConfirmation.cardStatus(),
                    userId
            );
            return new ConfirmActionResponseDto(
                    confirmationToken,
                    pendingConfirmation.actionType(),
                    cardStatusResult.status(),
                    "Updated card " + cardStatusResult.cardId() + " to status " + cardStatusResult.status(),
                    Instant.now()
            );
        }

        throw new IllegalStateException("Unsupported confirmation action: " + pendingConfirmation.actionType());
    }

    @Override
    public List<ChatTurnResponseDto> getSessionMemory(String userId, String sessionId) {
        return chatMemoryRepository.findRecentTurns(userId, sessionId, properties.shortTermMemoryWindow())
                .stream()
                .map(turn -> new ChatTurnResponseDto(
                        turn.id(),
                        turn.userId(),
                        turn.sessionId(),
                        turn.role(),
                        turn.content(),
                        turn.timestamp()
                ))
                .toList();
    }

    private void streamReplyTokens(String userId, String sessionId, String messageId, String replyText) {
        String[] replyWords = replyText.split("\\s+");
        for (int wordIndex = 0; wordIndex < replyWords.length; wordIndex++) {
            boolean isFinalToken = (wordIndex == replyWords.length - 1);
            String tokenChunk = replyWords[wordIndex] + (isFinalToken ? "" : " ");
            streamPublisher.publishStreamChunk(userId, sessionId, messageId, tokenChunk, isFinalToken);
        }
    }
}

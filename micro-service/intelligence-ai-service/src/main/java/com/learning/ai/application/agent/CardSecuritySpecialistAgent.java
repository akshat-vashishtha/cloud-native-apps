package com.learning.ai.application.agent;

import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentExecutionContext;
import com.learning.ai.infrastructure.tools.CardSecurityTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CardSecuritySpecialistAgent implements SpecialistAiAgent {

    private static final Logger log = LoggerFactory.getLogger(CardSecuritySpecialistAgent.class);

    private final ChatClient securityChatClient;
    private final CardSecurityTools cardSecurityTools;
    private final Resource systemPromptResource;

    public CardSecuritySpecialistAgent(
            @Qualifier("securityChatClient") ChatClient securityChatClient,
            CardSecurityTools cardSecurityTools,
            @Value("classpath:prompts/security-agent-system.st") Resource systemPromptResource
    ) {
        this.securityChatClient = securityChatClient;
        this.cardSecurityTools = cardSecurityTools;
        this.systemPromptResource = systemPromptResource;
    }

    @Override
    public AgentCategory supportedCategory() {
        return AgentCategory.CARD_SECURITY;
    }

    @Override
    public String execute(AgentExecutionContext executionContext) {
        try {
            String assistantResponse = securityChatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID,
                            executionContext.conversationId()
                    ))
                    .system(systemSpec -> systemSpec.text(systemPromptResource)
                            .param("userId", executionContext.userId())
                            .param("memoryContext", executionContext.memoryContext()))
                    .user(executionContext.userPrompt())
                    .tools(cardSecurityTools)
                    .call()
                    .content();
            if (assistantResponse != null && !assistantResponse.isBlank()) {
                return assistantResponse;
            }
        } catch (Exception exception) {
            log.warn("CardSecuritySpecialistAgent fallback activated: {}", exception.getMessage());
        }
        return "Card Security Specialist Agent ready. Provide your cardId and desired status (LOCKED/ACTIVE) to stage a 2-step HITL confirmation.";
    }
}

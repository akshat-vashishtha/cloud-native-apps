package com.learning.ai.application.agent;

import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentExecutionContext;
import com.learning.ai.infrastructure.tools.TransactionOperationTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class TransactionSpecialistAgent implements SpecialistAiAgent {

    private static final Logger log = LoggerFactory.getLogger(TransactionSpecialistAgent.class);

    private final ChatClient transactionChatClient;
    private final TransactionOperationTools transactionOperationTools;
    private final Resource systemPromptResource;

    public TransactionSpecialistAgent(
            @Qualifier("transactionChatClient") ChatClient transactionChatClient,
            TransactionOperationTools transactionOperationTools,
            @Value("classpath:prompts/transaction-agent-system.st") Resource systemPromptResource
    ) {
        this.transactionChatClient = transactionChatClient;
        this.transactionOperationTools = transactionOperationTools;
        this.systemPromptResource = systemPromptResource;
    }

    @Override
    public AgentCategory supportedCategory() {
        return AgentCategory.TRANSACTION_EXECUTION;
    }

    @Override
    public String execute(AgentExecutionContext executionContext) {
        try {
            String assistantResponse = transactionChatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID,
                            executionContext.conversationId()
                    ))
                    .system(systemSpec -> systemSpec.text(systemPromptResource)
                            .param("userId", executionContext.userId())
                            .param("memoryContext", executionContext.memoryContext()))
                    .user(executionContext.userPrompt())
                    .tools(transactionOperationTools)
                    .call()
                    .content();
            if (assistantResponse != null && !assistantResponse.isBlank()) {
                return assistantResponse;
            }
        } catch (Exception exception) {
            log.warn("TransactionSpecialistAgent fallback activated: {}", exception.getMessage());
        }
        return "Transaction Specialist Agent ready. Provide sourceAccountId, targetAccountId, and amount to stage a 2-step HITL transfer confirmation.";
    }
}

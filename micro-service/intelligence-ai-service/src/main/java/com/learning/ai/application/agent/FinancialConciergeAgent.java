package com.learning.ai.application.agent;

import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentExecutionContext;
import com.learning.ai.infrastructure.rag.BankingKnowledgeRagService;
import com.learning.ai.infrastructure.tools.ConciergeKnowledgeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FinancialConciergeAgent implements SpecialistAiAgent {

    private static final Logger log = LoggerFactory.getLogger(FinancialConciergeAgent.class);

    private final ChatClient conciergeChatClient;
    private final ConciergeKnowledgeTools conciergeKnowledgeTools;
    private final BankingKnowledgeRagService ragService;
    private final Resource systemPromptResource;

    public FinancialConciergeAgent(
            @Qualifier("conciergeChatClient") ChatClient conciergeChatClient,
            ConciergeKnowledgeTools conciergeKnowledgeTools,
            BankingKnowledgeRagService ragService,
            @Value("classpath:prompts/concierge-agent-system.st") Resource systemPromptResource
    ) {
        this.conciergeChatClient = conciergeChatClient;
        this.conciergeKnowledgeTools = conciergeKnowledgeTools;
        this.ragService = ragService;
        this.systemPromptResource = systemPromptResource;
    }

    @Override
    public AgentCategory supportedCategory() {
        return AgentCategory.CONCIERGE_FAQ;
    }

    @Override
    public String execute(AgentExecutionContext executionContext) {
        String ragContext = ragService.retrieveFormattedContext(executionContext.userPrompt(), 3);
        try {
            String assistantResponse = conciergeChatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID,
                            executionContext.conversationId()
                    ))
                    .system(systemSpec -> systemSpec.text(systemPromptResource)
                            .param("userId", executionContext.userId())
                            .param("ragContext", ragContext)
                            .param("memoryContext", executionContext.memoryContext()))
                    .user(executionContext.userPrompt())
                    .tools(conciergeKnowledgeTools)
                    .call()
                    .content();
            if (assistantResponse != null && !assistantResponse.isBlank()) {
                return assistantResponse;
            }
        } catch (Exception exception) {
            log.warn("FinancialConciergeAgent fallback activated: {}", exception.getMessage());
        }
        return "Sophia (Financial Concierge): Based on our policies:\n" + ragContext;
    }
}

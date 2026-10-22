package com.learning.ai.application.agent;

import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class IntentRouterAgent {

    private static final Logger log = LoggerFactory.getLogger(IntentRouterAgent.class);

    private final ChatClient routerChatClient;
    private final Resource routerSystemPrompt;

    public IntentRouterAgent(
            @Qualifier("routerChatClient") ChatClient routerChatClient,
            @Value("classpath:prompts/router-system.st") Resource routerSystemPrompt
    ) {
        this.routerChatClient = routerChatClient;
        this.routerSystemPrompt = routerSystemPrompt;
    }

    public AgentCategory classifyIntent(String prompt) {
        try {
            AgentClassification classification = routerChatClient.prompt()
                    .system(routerSystemPrompt)
                    .user(prompt)
                    .call()
                    .entity(AgentClassification.class);
            if (classification != null && classification.category() != null) {
                log.info("IntentRouterAgent classified prompt into [{}] (reason: {})",
                        classification.category(), classification.reasoning());
                return classification.category();
            }
        } catch (Exception exception) {
            log.debug("LLM router fallback to deterministic keyword classification: {}", exception.getMessage());
        }

        String lower = prompt.toLowerCase();
        if (lower.contains("card") || lower.contains("lock") || lower.contains("unlock")) {
            return AgentCategory.CARD_SECURITY;
        }
        if (lower.contains("transfer") || lower.contains("balance") || lower.contains("send") || lower.contains("account")) {
            return AgentCategory.TRANSACTION_EXECUTION;
        }
        return AgentCategory.CONCIERGE_FAQ;
    }
}

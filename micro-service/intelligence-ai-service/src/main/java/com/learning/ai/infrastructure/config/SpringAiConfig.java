package com.learning.ai.infrastructure.config;

import com.learning.ai.infrastructure.observability.AiAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.mongo.MongoChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
public class SpringAiConfig {

    private static final List<String> PROMPT_INJECTION_DENYLIST = List.of(
            "ignore previous instructions",
            "ignore all previous instructions",
            "reveal system prompt",
            "bypass confirmation",
            "execute raw sql"
    );

    @Bean
    public ChatMemoryRepository mongoChatMemoryRepository(MongoTemplate mongoTemplate) {
        return MongoChatMemoryRepository.builder()
                .mongoTemplate(mongoTemplate)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(
            ChatMemoryRepository mongoChatMemoryRepository,
            AiServiceProperties properties
    ) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(mongoChatMemoryRepository)
                .maxMessages(properties.shortTermMemoryWindow())
                .build();
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor() {
        return new SafeGuardAdvisor(
                PROMPT_INJECTION_DENYLIST,
                "Request blocked by OWASP LLM01 SafeGuard policy: prompt injection or guardrail bypass detected.",
                0
        );
    }

    @Bean("routerChatClient")
    public ChatClient routerChatClient(
            ChatClient.Builder builder,
            SafeGuardAdvisor safeGuardAdvisor,
            AiAuditAdvisor aiAuditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(safeGuardAdvisor, aiAuditAdvisor)
                .build();
    }

    @Bean("conciergeChatClient")
    public ChatClient conciergeChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            SafeGuardAdvisor safeGuardAdvisor,
            AiAuditAdvisor aiAuditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(
                        safeGuardAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(),
                        aiAuditAdvisor
                )
                .build();
    }

    @Bean("transactionChatClient")
    public ChatClient transactionChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            SafeGuardAdvisor safeGuardAdvisor,
            AiAuditAdvisor aiAuditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(
                        safeGuardAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(),
                        aiAuditAdvisor
                )
                .build();
    }

    @Bean("securityChatClient")
    public ChatClient securityChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            SafeGuardAdvisor safeGuardAdvisor,
            AiAuditAdvisor aiAuditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(
                        safeGuardAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(),
                        aiAuditAdvisor
                )
                .build();
    }

    @Bean("evaluatorChatClient")
    public ChatClient evaluatorChatClient(
            ChatClient.Builder builder,
            AiAuditAdvisor aiAuditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(aiAuditAdvisor)
                .build();
    }
}

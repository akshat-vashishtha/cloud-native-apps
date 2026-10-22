package com.learning.ai.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class AiAuditAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(AiAuditAdvisor.class);

    private final MeterRegistry meterRegistry;

    public AiAuditAdvisor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String requestId = UUID.randomUUID().toString();
        long startNanos = System.nanoTime();

        try {
            ChatClientResponse response = chain.nextCall(request);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

            Timer.builder("ai.prompt.latency")
                    .tag("status", "SUCCESS")
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);

            ChatResponse chatResponse = response.chatResponse();
            if (chatResponse != null && chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                int inputTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                int outputTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;

                Counter.builder("ai.tokens.used").tag("type", "input").register(meterRegistry).increment(inputTokens);
                Counter.builder("ai.tokens.used").tag("type", "output").register(meterRegistry).increment(outputTokens);

                log.info("[AI-AUDIT] requestId={} latencyMs={} inputTokens={} outputTokens={}",
                        requestId, durationMs, inputTokens, outputTokens);
            } else {
                log.info("[AI-AUDIT] requestId={} latencyMs={}", requestId, durationMs);
            }
            return response;
        } catch (Exception exception) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            Timer.builder("ai.prompt.latency")
                    .tag("status", "ERROR")
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);
            log.warn("[AI-AUDIT] requestId={} FAILED after {}ms: {}", requestId, durationMs, exception.getMessage());
            throw exception;
        }
    }

    @Override
    public String getName() {
        return "AiAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}

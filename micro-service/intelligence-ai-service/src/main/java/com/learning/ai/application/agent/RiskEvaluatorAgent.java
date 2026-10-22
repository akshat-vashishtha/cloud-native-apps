package com.learning.ai.application.agent;

import com.learning.ai.domain.model.PolicyEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RiskEvaluatorAgent {

    private static final Logger log = LoggerFactory.getLogger(RiskEvaluatorAgent.class);

    private final ChatClient evaluatorChatClient;
    private final Resource evaluatorSystemPrompt;

    private static final int MAX_REFINEMENT_ITERATIONS = 2;

    public RiskEvaluatorAgent(
            @Qualifier("evaluatorChatClient") ChatClient evaluatorChatClient,
            @Value("classpath:prompts/evaluator-agent-system.st") Resource evaluatorSystemPrompt
    ) {
        this.evaluatorChatClient = evaluatorChatClient;
        this.evaluatorSystemPrompt = evaluatorSystemPrompt;
    }

    public String verifyAndRefineResponse(String userPrompt, String candidateReply) {
        String currentReply = candidateReply;
        try {
            for (int iteration = 1; iteration <= MAX_REFINEMENT_ITERATIONS; iteration++) {
                PolicyEvaluation evaluation = evaluatorChatClient.prompt()
                        .system(evaluatorSystemPrompt)
                        .user("Customer Prompt: " + userPrompt + "\nCandidate Assistant Reply: " + currentReply)
                        .call()
                        .entity(PolicyEvaluation.class);

                if (evaluation == null || evaluation.acceptable()) {
                    return currentReply;
                }

                log.warn("RiskEvaluatorAgent flagged reply on iteration {}/{}; refining with feedback: {}",
                        iteration, MAX_REFINEMENT_ITERATIONS, evaluation.feedback());

                String refined = evaluatorChatClient.prompt()
                        .system("Rewrite the assistant reply to strictly resolve this compliance feedback: " + evaluation.feedback()
                                + ". Return only the refined response without meta-commentary.")
                        .user(currentReply)
                        .call()
                        .content();

                if (refined != null && !refined.isBlank()) {
                    currentReply = refined.trim();
                } else {
                    break;
                }
            }
        } catch (Exception exception) {
            log.debug("RiskEvaluatorAgent evaluation fallback: {}", exception.getMessage());
        }
        return currentReply;
    }
}

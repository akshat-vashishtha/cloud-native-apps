package com.learning.ai.infrastructure.tools;

import com.learning.ai.infrastructure.grpc.GrpcUserRbacClient;
import com.learning.ai.infrastructure.rag.BankingKnowledgeRagService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ConciergeKnowledgeTools {

    private final GrpcUserRbacClient userRbacClient;
    private final BankingKnowledgeRagService ragService;

    public ConciergeKnowledgeTools(
            GrpcUserRbacClient userRbacClient,
            BankingKnowledgeRagService ragService
    ) {
        this.userRbacClient = userRbacClient;
        this.ragService = ragService;
    }

    @Tool(description = "Read-only tool to fetch the authenticated customer's profile, roles, and permissions via gRPC")
    public GrpcUserRbacClient.UserProfileSummary getCustomerProfile(
            @ToolParam(description = "The authenticated user ID") String userId
    ) {
        return userRbacClient.getUserProfile(userId);
    }

    @Tool(description = "Search the official Digital Banking policy knowledge base for limits, fraud rules, and card policies")
    public String searchBankingPolicyKnowledgeBase(
            @ToolParam(description = "Policy search query") String query
    ) {
        return ragService.retrieveFormattedContext(query, 3);
    }
}

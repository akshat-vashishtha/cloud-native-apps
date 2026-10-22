package com.learning.ai.infrastructure.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BankingKnowledgeRagService {

    private static final Logger log = LoggerFactory.getLogger(BankingKnowledgeRagService.class);

    private final VectorStore qdrantVectorStore;

    private final List<Document> seededKnowledgeDocuments = List.of(
            new Document(
                    "doc_transfer_limits",
                    "Standard checking and savings accounts have a daily wire and P2P transfer limit of $5,000 USD. Premium tier accounts (ROLE_PREMIUM) have an elevated daily transfer limit of $25,000 USD per 24-hour window.",
                    Map.of("category", "TRANSFER_POLICY", "tier", "ALL")
            ),
            new Document(
                    "doc_fraud_velocity",
                    "Real-time fraud velocity protection monitors outgoing transfers in a 5-minute sliding window. Executing 3 or more transfers within 5 minutes automatically triggers a HIGH_VELOCITY_TRANSFERS alert.",
                    Map.of("category", "SECURITY_POLICY", "tier", "ALL")
            ),
            new Document(
                    "doc_hitl_security",
                    "All AI-initiated financial mutations (fund transfers and debit/credit card status changes) require 2-step Human-in-the-Loop (HITL) confirmation via a time-bound confirmation token delivered over STOMP WebSockets.",
                    Map.of("category", "AI_GOVERNANCE", "tier", "ALL")
            ),
            new Document(
                    "doc_card_controls",
                    "Customers can temporarily lock (LOCKED) or unlock (ACTIVE) their debit or credit cards at any time. A locked card declines new point-of-sale and online authorizations while recurring bills continue.",
                    Map.of("category", "CARD_POLICY", "tier", "ALL")
            )
    );

    public BankingKnowledgeRagService(ObjectProvider<VectorStore> vectorStoreProvider) {
        this.qdrantVectorStore = vectorStoreProvider.getIfAvailable();
    }

    @PostConstruct
    public void seedVectorStore() {
        if (qdrantVectorStore != null) {
            try {
                log.info("Ingesting {} seeded banking policy documents into Qdrant VectorStore...", seededKnowledgeDocuments.size());
                qdrantVectorStore.accept(seededKnowledgeDocuments);
                log.info("Successfully seeded banking policy documents into VectorStore.");
            } catch (Exception exception) {
                log.warn("VectorStore seeding skipped (remote store not ready or read-only): {}", exception.getMessage());
            }
        }
    }

    public List<Document> retrieveRelevantDocuments(String searchQuery, int maxResults) {
        if (qdrantVectorStore != null) {
            try {
                SearchRequest vectorSearchRequest = SearchRequest.builder()
                        .query(searchQuery)
                        .topK(maxResults)
                        .similarityThreshold(0.65)
                        .build();
                List<Document> vectorMatches = qdrantVectorStore.similaritySearch(vectorSearchRequest);
                if (vectorMatches != null && !vectorMatches.isEmpty()) {
                    return vectorMatches;
                }
            } catch (Exception exception) {
                log.debug("Qdrant vector store similarity search unavailable, falling back to seeded policy documents: {}",
                        exception.getMessage());
            }
        }

        Set<String> normalizedQueryTerms = Arrays.stream(searchQuery.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());

        Comparator<Document> relevanceComparator = (firstDocument, secondDocument) -> Integer.compare(
                calculateLexicalRelevanceScore(secondDocument, normalizedQueryTerms),
                calculateLexicalRelevanceScore(firstDocument, normalizedQueryTerms)
        );

        return seededKnowledgeDocuments.stream()
                .sorted(relevanceComparator)
                .limit(maxResults)
                .toList();
    }

    public String retrieveFormattedContext(String searchQuery, int maxResults) {
        return retrieveRelevantDocuments(searchQuery, maxResults).stream()
                .map(knowledgeDocument -> "- ["
                        + knowledgeDocument.getMetadata().getOrDefault("category", "POLICY")
                        + "] "
                        + knowledgeDocument.getText())
                .collect(Collectors.joining("\n"));
    }

    private int calculateLexicalRelevanceScore(Document knowledgeDocument, Set<String> normalizedQueryTerms) {
        String documentContent = (knowledgeDocument.getText() != null ? knowledgeDocument.getText() : "").toLowerCase();
        int matchingTermCount = 0;
        for (String queryTerm : normalizedQueryTerms) {
            if (documentContent.contains(queryTerm)) {
                matchingTermCount++;
            }
        }
        return matchingTermCount;
    }
}

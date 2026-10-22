package com.learning.ai.infrastructure.persistence.criteria;

import com.learning.ai.domain.model.ChatTurn;
import com.learning.ai.infrastructure.persistence.document.ChatTurnDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ChatMemoryCriteriaRepository {

    private final MongoTemplate mongoTemplate;

    public ChatMemoryCriteriaRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void appendTurn(ChatTurn chatTurn) {
        ChatTurnDocument chatTurnDocument = ChatTurnDocument.builder()
                .id(chatTurn.id())
                .userId(chatTurn.userId())
                .sessionId(chatTurn.sessionId())
                .role(chatTurn.role())
                .content(chatTurn.content())
                .timestamp(chatTurn.timestamp())
                .build();
        mongoTemplate.save(chatTurnDocument);
    }

    public List<ChatTurn> findRecentTurns(String userId, String sessionId, int maxTurns) {
        Criteria sessionCriteria = Criteria.where("userId").is(userId)
                .and("sessionId").is(sessionId);

        Query recentTurnsQuery = Query.query(sessionCriteria)
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(maxTurns);

        List<ChatTurnDocument> turnDocuments = new ArrayList<>(
                mongoTemplate.find(recentTurnsQuery, ChatTurnDocument.class)
        );
        Collections.reverse(turnDocuments);

        return turnDocuments.stream()
                .map(turnDocument -> new ChatTurn(
                        turnDocument.getId(),
                        turnDocument.getUserId(),
                        turnDocument.getSessionId(),
                        turnDocument.getRole(),
                        turnDocument.getContent(),
                        turnDocument.getTimestamp()
                ))
                .toList();
    }
}

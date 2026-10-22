package com.learning.ai.domain.model;

import com.learning.ai.domain.enums.AgentCategory;

public record AgentClassification(
        AgentCategory category,
        String reasoning
) {}

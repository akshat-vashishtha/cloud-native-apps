package com.learning.ai.domain.model;

public record PolicyEvaluation(
        boolean acceptable,
        String feedback
) {}

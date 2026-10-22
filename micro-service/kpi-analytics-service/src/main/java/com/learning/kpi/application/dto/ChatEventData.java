package com.learning.kpi.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatEventData(
        String id,
        String channelId,
        String senderId,
        String recipientId,
        String content,
        String messageType,
        Instant timestamp
) {}

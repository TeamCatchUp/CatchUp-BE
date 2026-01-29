package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.entity.ChatHistory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserQueryHistoryResponse(
        UUID sessionId,
        String query,
        LocalDateTime createdAt,
        Long chatHistoryId,
        Boolean hasFeedback
) {
    public static UserQueryHistoryResponse from(ChatHistory history, Boolean hasFeedback) {
        return UserQueryHistoryResponse.builder()
                .sessionId(history.getChatRoom().getSessionId())
                .query(history.getContent())
                .createdAt(history.getCreatedAt())
                .chatHistoryId(history.getId())
                .hasFeedback(hasFeedback)
                .build();
    }
}

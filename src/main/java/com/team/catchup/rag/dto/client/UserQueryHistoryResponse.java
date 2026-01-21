package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.entity.ChatHistory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserQueryHistoryResponse(
        String query,
        LocalDateTime createdAt
) {
    public static UserQueryHistoryResponse from(ChatHistory history){
        return UserQueryHistoryResponse.builder()
                .query(history.getContent())
                .createdAt(history.getCreatedAt())
                .build();
    }
}

package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team.catchup.rag.entity.ChatHistory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
public record ChatHistoryResponse(
        Long id,
        String role, // user 또는 assistant
        String content,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<ClientSource> sources,

        LocalDateTime createdAt
) {
    public static ChatHistoryResponse from(ChatHistory history) {
        // User 채팅인 경우 Source 부재
        List<ClientSource> clientSources = Collections.emptyList();

        if (history.getMetadata() != null && history.getMetadata().serverSources() != null) {
            clientSources = history.getMetadata().serverSources().stream()
                    .map(ClientSource::from)
                    .toList();
        }

        return ChatHistoryResponse.builder()
                .id(history.getId())
                .role(history.getRole())
                .content(history.getContent())
                .sources(clientSources)
                .createdAt(history.getCreatedAt())
                .build();
    }
}

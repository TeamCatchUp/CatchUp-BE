package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ServerChatRequest(
        @NotBlank String query,
        String role, // 페르소나
        @NotNull UUID session_id,
        @NotBlank String index_name
) {
    public static ServerChatRequest of(String query,
                                       String role,
                                       UUID sessionId,
                                       String index_name
    ){
        return ServerChatRequest.builder()
                .query(query)
                .role(role)
                .session_id(sessionId)
                .index_name(index_name)
                .build();
    }
}

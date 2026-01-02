package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Builder
public record ServerChatRequest(
        @NotBlank String query,
        String role, // 페르소나
        @NotNull UUID session_id
) {
    public static ServerChatRequest of(String query,
                                       String role,
                                       UUID sessionId
    ){
        return ServerChatRequest.builder()
                .query(query)
                .role(role)
                .session_id(sessionId)
                .build();
    }
}

package com.team.catchup.rag.dto.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * Spring -> FastAPI 채팅 요청 dto
 */

@Builder
public record ServerChatRequest(
        @NotBlank String query,
        String role, // 페르소나
        @NotNull UUID session_id,
        @NotNull List<String> index_list
) {
    public static ServerChatRequest of(String query,
                                       String role,
                                       UUID sessionId,
                                       List<String> indexList
    ) {
        return ServerChatRequest.builder()
                .query(query)
                .role(role)
                .session_id(sessionId)
                .index_list(indexList)
                .build();
    }
}

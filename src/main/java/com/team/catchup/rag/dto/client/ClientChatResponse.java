package com.team.catchup.rag.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Client에게 전달할 LLM이 생성한 최종 답변과 출처 목록
 */

public record ClientChatResponse(
        @NotNull UUID sessionId,
        @NotBlank String answer,
        List<ClientSource> sources
) {
    public static ClientChatResponse createFinalResponse(
            UUID sessionId,
            String answer,
            List<ClientSource> clientSources
    ) {
        return new ClientChatResponse(sessionId, answer, clientSources);
    }

    public static ClientChatResponse createInfoResponse(UUID sessionId, String answer) {
        return new ClientChatResponse(sessionId, answer, List.of());
    }
}

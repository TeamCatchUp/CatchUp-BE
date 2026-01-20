package com.team.catchup.rag.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Client -> Spring 채팅 요청 dto
 */

public record ClientChatRequest(
        @NotBlank String query,
        @NotNull UUID sessionId,
        @NotNull List<String> indexList // 중간 시연용
) {
}

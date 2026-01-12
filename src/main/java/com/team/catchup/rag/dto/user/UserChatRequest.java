package com.team.catchup.rag.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UserChatRequest(
        @NotBlank String query,
        @NotNull UUID sessionId,
        @NotNull List<String> indexList // 중간 시연용
) {
}

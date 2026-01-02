package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserChatRequest(
        @NotBlank String query,
        @NotNull UUID sessionId
        ) {
}

package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserChatResponse(
        @NotBlank String answer
) {
    public static UserChatResponse of(String answer, UUID sessionId) {
        return new UserChatResponse(answer);
    }
}

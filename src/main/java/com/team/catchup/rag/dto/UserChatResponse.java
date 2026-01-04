package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record UserChatResponse(
        @NotBlank String answer
) {
}

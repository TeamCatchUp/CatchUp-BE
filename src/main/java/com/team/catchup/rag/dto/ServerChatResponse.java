package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record ServerChatResponse(
        @NotBlank String answer,
        List<Source> sources,
        String modelName // TODO: FastAPI 쪽에서 호환 필요
) {
    public static ServerChatResponse createError(String errorMessage) {
        return ServerChatResponse.builder()
                .answer(errorMessage)
                .sources(List.of())
                .modelName("System")
                .build();
    }
}
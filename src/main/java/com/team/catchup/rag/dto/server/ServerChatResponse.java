package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record ServerChatResponse(
        @NotBlank String answer,

        List<Source> sources,

        @JsonProperty("process_time")
        Double processTime
) {
    public static ServerChatResponse createError(String errorMessage) {
        return ServerChatResponse.builder()
                .answer(errorMessage)
                .sources(List.of())
                .build();
    }
}
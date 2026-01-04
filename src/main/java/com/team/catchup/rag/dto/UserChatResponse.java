package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UserChatResponse(
        @NotNull UUID sesionId,
        @NotBlank String answer,
        List<Source> sources
) {
    public record Source(
            String sourceType,
            String content,
            String filePath,
            String htmlUrl,
            String language
    ){}

    public static UserChatResponse from(
            UUID sessionId,
            ServerChatResponse serverResponse
    ) {
        List<Source> clientSources = serverResponse.sources().stream()
                .map(s -> new Source(
                        s.sourceType(),
                        s.content(),
                        s.filePath(),
                        s.htmlUrl(),
                        s.language()
                ))
                .toList();

        return new UserChatResponse(
                sessionId,
                serverResponse.answer(),
                clientSources
        );
    }

    public static UserChatResponse of(UUID sessionId, String answer) {
        return new UserChatResponse(sessionId, answer, List.of());
    }
}

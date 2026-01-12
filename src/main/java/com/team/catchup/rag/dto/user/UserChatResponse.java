package com.team.catchup.rag.dto.user;

import com.team.catchup.rag.dto.server.ServerChatResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UserChatResponse(
        @NotNull UUID sesionId,
        @NotBlank String answer,
        List<ClientSource> sources
) {
    public static UserChatResponse from(
            UUID sessionId,
            ServerChatResponse serverResponse
    ) {
        List<ClientSource> clientSources = serverResponse.sources().stream()
                .map(ClientSource::from)
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

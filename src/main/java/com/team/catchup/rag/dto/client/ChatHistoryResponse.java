package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ChatHistoryResponse(
        Long id,
        String role, // user 또는 assistant
        String content,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<ClientSource> sources,

        LocalDateTime createdAt
) {}

package com.team.catchup.rag.dto.client;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatFeedbackRequest(
        @NotNull Long chatHistoryId,
        @NotEmpty List<String> tags,
        @Size(max = 500) String detail
) {
}

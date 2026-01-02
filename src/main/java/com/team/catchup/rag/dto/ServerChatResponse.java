package com.team.catchup.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record ServerChatResponse(
        @NotBlank String answer,
        Source sources
) {
    private record Source (
            String source,
            String category,
            Integer page,
            String content,
            String file_path,
            String file_name
    ){
    }
}


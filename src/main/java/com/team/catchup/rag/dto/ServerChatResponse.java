package com.team.catchup.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ServerChatResponse(
        @NotBlank String answer,
        List<Source> sources
) {
    public record Source (
            @JsonProperty("source_type")
            String sourceType,

            @JsonProperty("text")
            String content,

            @JsonProperty("file_path")
            String filePath,

            String category,

            String source, // 문서 출처

            @JsonProperty("html_url")
            String htmlUrl,

            String language
    ){
    }
}
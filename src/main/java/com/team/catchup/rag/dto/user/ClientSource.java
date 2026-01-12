package com.team.catchup.rag.dto.user; // 패키지 분리 권장

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.Source;
import lombok.Builder;

@Builder
public record ClientSource(
        Integer index,

        @JsonProperty("is_cited")
        Boolean isCited,

        @JsonProperty("source_type")
        String sourceType,

        @JsonProperty("text")
        String content,

        @JsonProperty("file_path")
        String filePath,

        String category,

        String source,

        @JsonProperty("html_url")
        String htmlUrl,

        String language
) {
    public static ClientSource from(Source source) {
        return ClientSource.builder()
                .index(source.index())
                .isCited(source.isCited())
                .sourceType(source.sourceType())
                .content(source.content())
                .filePath(source.filePath())
                .category(source.category())
                .source(source.source())
                .htmlUrl(source.htmlUrl())
                .language(source.language())
                .build();
    }
}
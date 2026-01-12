package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Source(
        Integer index,  // LLM이 답변에 직접 인용한 문서 번호

        @JsonProperty("is_cited")
        Boolean isCited, // LLM이 답변에 활용했는지 여부

        @JsonProperty("relevance_score")
        Double relevanceScore,

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
) {
        public static Source createClientSource(Source source){
                return Source.builder()
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
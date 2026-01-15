package com.team.catchup.rag.dto.client; // 패키지 분리 권장

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.ServerSource;
import lombok.Builder;

/**
 * Spring -> Client 최종 답변 생성시 함께 제공되는 출처 관련 메타데이터
 */

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
    public static ClientSource from(ServerSource serverSource) {
        return ClientSource.builder()
                .index(serverSource.index())
                .isCited(serverSource.isCited())
                .sourceType(serverSource.sourceType())
                .content(serverSource.content())
                .filePath(serverSource.filePath())
                .category(serverSource.category())
                .source(serverSource.source())
                .htmlUrl(serverSource.htmlUrl())
                .language(serverSource.language())
                .build();
    }
}
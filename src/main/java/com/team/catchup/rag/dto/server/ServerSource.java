package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * FastAPI -> Spring 최종 답변 생성 시에 함께 돌아오는 출처 관련 메타데이터
 */

@Builder
public record ServerSource(
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
}
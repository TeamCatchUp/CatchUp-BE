package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * FastAPI -> Spring 최종 답변 생성 시에 함께 돌아오는 출처 관련 공통 메타데이터
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "source_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServerCodeSource.class, name = "0"),
        @JsonSubTypes.Type(value = ServerPullRequestSource.class, name = "1"),
})
public abstract class ServerSource {
    private Integer index; // LLM이 답변에 직접인용한 문서 번호

    @JsonProperty("is_cited")
    private Boolean isCited;

    @JsonProperty("source_type")
    private Integer sourceType;

    @JsonProperty("relevance_score")
    private Double relevanceScore;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("text")
    private String text;
}
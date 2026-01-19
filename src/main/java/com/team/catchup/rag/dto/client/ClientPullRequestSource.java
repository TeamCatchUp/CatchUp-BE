package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.ServerPullRequestSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ClientPullRequestSource extends ClientSource{

    private String title;

    @JsonProperty("pr_number")
    private Integer prNumber;

    private String state;

    @JsonProperty("created_at")
    private Long createdAt;

    private String author;

    public static ClientPullRequestSource from (ServerPullRequestSource source) {
        return ClientPullRequestSource.builder()
                // 공통 필드
                .index(source.getIndex())
                .isCited(source.getIsCited())
                .sourceType(source.getSourceType())
                .relevanceScore(source.getRelevanceScore())
                .htmlUrl(source.getHtmlUrl())
                .content(source.getText())
                // PR 전용
                .title(source.getTitle())
                .prNumber(source.getPrNumber())
                .state(source.getState())
                .createdAt(source.getCreatedAt())
                .author(source.getAuthor())
                .build();
    }
}

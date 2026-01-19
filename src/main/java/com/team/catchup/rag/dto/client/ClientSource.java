package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import com.team.catchup.rag.dto.server.ServerPullRequestSource;
import com.team.catchup.rag.dto.server.ServerSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientSource {

    private Integer index;

    @JsonProperty("is_cited")
    private Boolean isCited;

    @JsonProperty("source_type")
    private Integer sourceType;

    @JsonProperty("relevance_score")
    private Double relevanceScore;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("text")
    private String content;

    public static ClientSource from(ServerSource serverSource) {
        if (serverSource instanceof ServerCodeSource codeSource) {
            return ClientCodeSource.from(codeSource);
        }

        else if (serverSource instanceof ServerPullRequestSource prSource) {
            return ClientPullRequestSource.from(prSource);
        }
        throw new IllegalArgumentException("지원하지 않는 Source Type 입니다.");
    }
}

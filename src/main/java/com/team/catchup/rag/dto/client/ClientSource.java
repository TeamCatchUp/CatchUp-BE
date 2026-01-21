package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import com.team.catchup.rag.dto.server.ServerJiraIssueSource;
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

    private Boolean isCited;

    private Integer sourceType;

    private Double relevanceScore;

    private String htmlUrl;

    private String content;

    private String owner;

    private String repo;

    public static ClientSource from(ServerSource serverSource) {
        if (serverSource instanceof ServerCodeSource codeSource) {
            return ClientCodeSource.from(codeSource);
        }

        else if (serverSource instanceof ServerPullRequestSource prSource) {
            return ClientPullRequestSource.from(prSource);
        }

        else if (serverSource instanceof ServerJiraIssueSource jiraIssueSource) {
            return ClientJiraIssueSource.from(jiraIssueSource);
        }
        throw new IllegalArgumentException("지원하지 않는 Source Type 입니다.");
    }
}

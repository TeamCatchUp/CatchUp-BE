package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubIssue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubIssueRabbitRequest {

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("repo")
    private String repoName;

    @JsonProperty("issue_number")
    private Integer issueNumber;

    @JsonProperty("github_token")
    private String githubToken;

    public static GithubIssueRabbitRequest from(GithubIssue issue) {
        return GithubIssueRabbitRequest.builder()
            .repositoryId(issue.getRepository().getRepositoryId())
            .owner(issue.getRepository().getOwner())
            .repoName(issue.getRepository().getName())
            .issueNumber(issue.getNumber())
            .githubToken(null) // TODO: Private 저장소용 토큰이 필요한 경우 추가
            .build();
    }
}

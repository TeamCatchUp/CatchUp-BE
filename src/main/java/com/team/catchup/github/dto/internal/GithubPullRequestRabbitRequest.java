package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubPullRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubPullRequestRabbitRequest {

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("repo")
    private String repoName;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("pr_number")
    private Integer prNumber;

    @JsonProperty("github_token")
    private String githubToken;

    public static GithubPullRequestRabbitRequest from(GithubPullRequest pullRequest) {
        return GithubPullRequestRabbitRequest.builder()
            .repositoryId(pullRequest.getRepository().getRepositoryId())
            .owner(pullRequest.getRepository().getOwner())
            .repoName(pullRequest.getRepository().getName())
            .branch(pullRequest.getBaseBranch())
            .prNumber(pullRequest.getNumber())
            .githubToken(null) // TODO: Private 저장소용 토큰이 필요한 경우 추가
            .build();
    }
}

package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubPullRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubPullRequestRabbitRequest {

    @JsonProperty("pull_request_id")
    private Long pullRequestId;

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author_login")
    private String authorLogin;

    @JsonProperty("status")
    private String status;

    @JsonProperty("html_url")
    private String htmlUrl;

    public static GithubPullRequestRabbitRequest from(GithubPullRequest pullRequest) {
        return GithubPullRequestRabbitRequest.builder()
            .pullRequestId(pullRequest.getPullRequestId())
            .repositoryId(pullRequest.getRepository().getRepositoryId())
            .repositoryName(pullRequest.getRepository().getOwner() + "/" + pullRequest.getRepository().getName())
            .number(pullRequest.getNumber())
            .title(pullRequest.getTitle())
            .authorLogin(pullRequest.getAuthorLogin())
            .status(pullRequest.getStatus().name())
            .htmlUrl(pullRequest.getHtmlUrl())
            .build();
    }
}

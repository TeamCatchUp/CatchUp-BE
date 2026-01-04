package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubIssue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubIssueRabbitRequest {

    @JsonProperty("issue_id")
    private Long issueId;

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

    @JsonProperty("is_pull_request")
    private Boolean isPullRequest;

    @JsonProperty("html_url")
    private String htmlUrl;

    public static GithubIssueRabbitRequest from(GithubIssue issue) {
        return GithubIssueRabbitRequest.builder()
            .issueId(issue.getIssueId())
            .repositoryId(issue.getRepository().getRepositoryId())
            .repositoryName(issue.getRepository().getOwner() + "/" + issue.getRepository().getName())
            .number(issue.getNumber())
            .title(issue.getTitle())
            .authorLogin(issue.getAuthorLogin())
            .status(issue.getStatus().name())
            .htmlUrl(issue.getHtmlUrl())
            .build();
    }
}

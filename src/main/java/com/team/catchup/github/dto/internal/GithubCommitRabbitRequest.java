package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubCommit;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubCommitRabbitRequest {

    @JsonProperty("commit_sha")
    private String commitSha;

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("message")
    private String message;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_email")
    private String authorEmail;

    @JsonProperty("html_url")
    private String htmlUrl;

    public static GithubCommitRabbitRequest from(GithubCommit commit) {
        return GithubCommitRabbitRequest.builder()
            .commitSha(commit.getSha())
            .repositoryId(commit.getRepository().getRepositoryId())
            .repositoryName(commit.getRepository().getOwner() + "/" + commit.getRepository().getName())
            .message(commit.getMessage())
            .authorName(commit.getAuthorName())
            .authorEmail(commit.getAuthorEmail())
            .htmlUrl(commit.getHtmlUrl())
            .build();
    }
}

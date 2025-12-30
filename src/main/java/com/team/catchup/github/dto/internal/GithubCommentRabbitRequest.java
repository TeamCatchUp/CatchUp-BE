package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubCommentRabbitRequest {

    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("comment_type")
    private String commentType;

    @JsonProperty("author_login")
    private String authorLogin;

    @JsonProperty("pull_request_number")
    private Integer pullRequestNumber;

    @JsonProperty("issue_number")
    private Integer issueNumber;

    @JsonProperty("commit_sha")
    private String commitSha;

    @JsonProperty("html_url")
    private String htmlUrl;

    public static GithubCommentRabbitRequest from(GithubComment comment) {
        return GithubCommentRabbitRequest.builder()
            .commentId(comment.getCommentId())
            .repositoryId(comment.getRepository().getRepositoryId())
            .repositoryName(comment.getRepository().getOwner() + "/" + comment.getRepository().getName())
            .commentType(comment.getCommentType().name())
            .authorLogin(comment.getAuthorLogin())
            .pullRequestNumber(comment.getPullRequest() != null ? comment.getPullRequest().getNumber() : null)
            .issueNumber(comment.getIssue() != null ? comment.getIssue().getNumber() : null)
            .commitSha(comment.getCommitSha())
            .htmlUrl(comment.getHtmlUrl())
            .build();
    }
}

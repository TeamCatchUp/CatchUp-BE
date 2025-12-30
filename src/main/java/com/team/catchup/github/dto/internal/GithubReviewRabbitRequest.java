package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubReview;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubReviewRabbitRequest {

    @JsonProperty("review_id")
    private Long reviewId;

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("pull_request_number")
    private Integer pullRequestNumber;

    @JsonProperty("reviewer_login")
    private String reviewerLogin;

    @JsonProperty("review_state")
    private String reviewState;

    @JsonProperty("html_url")
    private String htmlUrl;

    public static GithubReviewRabbitRequest from(GithubReview review) {
        return GithubReviewRabbitRequest.builder()
            .reviewId(review.getReviewId())
            .repositoryId(review.getRepository().getRepositoryId())
            .repositoryName(review.getRepository().getOwner() + "/" + review.getRepository().getName())
            .pullRequestNumber(review.getPullRequest().getNumber())
            .reviewerLogin(review.getReviewerLogin())
            .reviewState(review.getReviewState().name())
            .htmlUrl(review.getHtmlUrl())
            .build();
    }
}

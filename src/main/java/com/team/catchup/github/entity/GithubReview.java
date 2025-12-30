package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubReview {

    // 리뷰 고유 ID
    @Id
    @Column(name = "review_id")
    private Long reviewId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // 소속 Pull Request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private GithubPullRequest pullRequest;

    // 리뷰어 GitHub 로그인 ID
    @Column(name = "reviewer_login")
    private String reviewerLogin;

    // 리뷰 상태 (APPROVED, CHANGES_REQUESTED, COMMENTED, DISMISSED, PENDING)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewState reviewState;

    // 리뷰 제출 시각
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // 인덱싱 완료 시각
    @Column(name = "indexed_at")
    private LocalDateTime indexedAt;

    // 리뷰 웹 URL
    @Column(name = "html_url")
    private String htmlUrl;

    public enum ReviewState {
        APPROVED,
        CHANGES_REQUESTED,
        COMMENTED,
        DISMISSED,
        PENDING
    }

    public void markAsIndexed() {
        this.indexedAt = LocalDateTime.now();
    }

    public boolean isIndexed() {
        return this.indexedAt != null;
    }
}

package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_pull_request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubPullRequest {

    // Pull Request 고유 ID
    @Id
    @Column(name = "pull_request_id")
    private Long pullRequestId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // Pull Request 번호
    @Column(nullable = false)
    private Integer number;

    // Pull Request 제목
    @Column(nullable = false, length = 1000)
    private String title;

    // Pull Request 상태 (OPEN, CLOSED, MERGED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PullRequestStatus status;

    // 작성자 GitHub 로그인 ID
    @Column(name = "author_login")
    private String authorLogin;

    // 머지 대상 브랜치
    @Column(name = "base_branch")
    private String baseBranch;

    // 머지 소스 브랜치
    @Column(name = "head_branch")
    private String headBranch;

    // 머지 커밋 SHA
    @Column(name = "merge_commit_sha", length = 40)
    private String mergeCommitSha;

    // Pull Request 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Pull Request 최종 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pull Request 종료 시각
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Pull Request 머지 시각
    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    // Pull Request 웹 URL
    @Column(name = "html_url")
    private String htmlUrl;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<GithubReview> reviews;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<GithubComment> comments;

    public enum PullRequestStatus {
        OPEN,
        CLOSED,
        MERGED
    }

    // Webhook 이벤트로 받은 정보로 PR 메타데이터 업데이트
    public void updateFromWebhook(String title, PullRequestStatus status, LocalDateTime updatedAt,
                                   LocalDateTime closedAt, LocalDateTime mergedAt, String mergeCommitSha) {
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.mergedAt = mergedAt;
        this.mergeCommitSha = mergeCommitSha;
    }
}

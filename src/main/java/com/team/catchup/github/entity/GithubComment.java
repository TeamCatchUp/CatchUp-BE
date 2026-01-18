package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubComment {

    // 댓글 고유 ID
    @Id
    @Column(name = "comment_id")
    private Long commentId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // 댓글 유형 (ISSUE_COMMENT, REVIEW_COMMENT, COMMIT_COMMENT)
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private CommentType commentType;

    // 소속 Pull Request (리뷰 댓글인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private GithubPullRequest pullRequest;

    // 소속 Issue (이슈 댓글인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private GithubIssue issue;

    // 커밋 SHA (커밋 댓글인 경우)
    @Column(name = "commit_sha", length = 40)
    private String commitSha;

    // 작성자 GitHub 로그인 ID
    @Column(name = "author_login")
    private String authorLogin;

    // 댓글 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 댓글 최종 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 댓글 URL
    @Column(name = "html_url")
    private String htmlUrl;

    public enum CommentType {
        ISSUE_COMMENT,
        REVIEW_COMMENT,
        COMMIT_COMMENT
    }
}

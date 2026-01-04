package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_issue")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubIssue {

    // Issue 고유 ID
    @Id
    @Column(name = "issue_id")
    private Long issueId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // Issue 번호
    @Column(nullable = false)
    private Integer number;

    // Issue 제목
    @Column(nullable = false, length = 1000)
    private String title;

    // Issue 상태 (OPEN, CLOSED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    // 작성자 GitHub ID
    @Column(name = "author_login")
    private String authorLogin;

    // Issue 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Issue 최종 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Issue 종료 시각
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // 인덱싱 완료 시각
    @Column(name = "indexed_at")
    private LocalDateTime indexedAt;

    // Issue URL
    @Column(name = "html_url")
    private String htmlUrl;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL)
    private List<GithubComment> comments;

    public enum IssueStatus {
        OPEN,
        CLOSED
    }

    public void markAsIndexed() {
        this.indexedAt = LocalDateTime.now();
    }

    public boolean isIndexed() {
        return this.indexedAt != null;
    }
}

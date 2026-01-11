package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_repository")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubRepository {

    // GitHub 리포지토리 고유 ID
    @Id
    @Column(name = "repository_id")
    private Long repositoryId;

    // 리포지토리 소유자 이름
    @Column(nullable = false)
    private String owner;

    // 리포지토리 이름
    @Column(nullable = false)
    private String name;

    // 리포지토리 설명
    @Column(length = 1000)
    private String description;

    // 기본 브랜치명
    @Column(name = "target_branch")
    private String targetBranch;

    // 주 사용 프로그래밍 언어
    @Column(name = "primary_language")
    private String primaryLanguage;

    // 비공개 리포지토리 여부
    @Column(name = "is_private")
    private Boolean isPrivate;

    // 리포지토리 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 리포지토리 최종 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 마지막 동기화 시각
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // 동기화 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SyncStatus syncStatus;

    // GitHub 리포지토리 웹 URL
    @Column(name = "html_url")
    private String htmlUrl;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL)
    private List<GithubCommit> commits;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL)
    private List<GithubPullRequest> pullRequests;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL)
    private List<GithubIssue> issues;

    public enum SyncStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public void updateSyncInfo(String branch, SyncStatus status) {
        this.targetBranch = branch;
        this.syncStatus = status;
        if (status == SyncStatus.COMPLETED) {
            this.lastSyncedAt = LocalDateTime.now();
        }
    }
}

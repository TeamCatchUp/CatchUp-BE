package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_file_change")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubFileChange {

    // 파일 변경 고유 ID (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_change_id")
    private Long fileChangeId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // 커밋 SHA
    @Column(name = "commit_sha", length = 40)
    private String commitSha;

    // 소속 Pull Request (PR에 포함된 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private GithubPullRequest pullRequest;

    // 파일 경로
    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    // 이전 파일 경로 (이름 변경된 경우)
    @Column(name = "previous_file_path", length = 1000)
    private String previousFilePath;

    // 변경 유형 (ADDED, MODIFIED, DELETED, RENAMED, COPIED)
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private FileChangeType changeType;

    // 추가된 라인 수
    @Column(name = "additions")
    private Integer additions;

    // 삭제된 라인 수
    @Column(name = "deletions")
    private Integer deletions;

    public enum FileChangeType {
        ADDED,
        MODIFIED,
        DELETED,
        RENAMED,
        COPIED
    }
}

package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_commit")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubCommit {

    // 커밋 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commit_id")
    private Long commitId;

    // 소속 리포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private GithubRepository repository;

    // 커밋 SHA (40자 해시값)
    @Column(nullable = false, unique = true, length = 40)
    private String sha;

    // 커밋 메시지
    @Column(length = 500)
    private String message;

    // 작성자 이름
    @Column(name = "author_name")
    private String authorName;

    // 작성자 이메일
    @Column(name = "author_email")
    private String authorEmail;

    // 작성 시각
    @Column(name = "author_date")
    private LocalDateTime authorDate;

    // 부모 커밋 목록
    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GithubCommitParent> parents;

    // 추가된 라인 수
    @Column(name = "additions")
    private Integer additions;

    // 삭제된 라인 수
    @Column(name = "deletions")
    private Integer deletions;

    // 커밋 웹 URL
    @Column(name = "html_url")
    private String htmlUrl;

    public void setParents(List<GithubCommitParent> parents) {
        this.parents = parents;
    }
}

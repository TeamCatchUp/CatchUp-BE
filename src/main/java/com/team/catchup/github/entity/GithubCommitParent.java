package com.team.catchup.github.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_commit_parent")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubCommitParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 자식 커밋
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false)
    private GithubCommit commit;

    // 부모 커밋 SHA
    @Column(name = "parent_sha", nullable = false, length = 40)
    private String parentSha;

    // 부모 순서 (0: 첫 번째 부모 - 현재 브랜치의 이전 커밋, 1: 두 번째 부모 - 머지된 브랜치의 커밋)
    // 일반 커밋: 부모가 1개만 존재하며 parent_order는 0
    // 머지 커밋: 부모가 2개 존재하며, 0은 main 브랜치의 이전 커밋, 1은 feature 브랜치의 마지막 커밋
    @Column(name = "parent_order", nullable = false)
    private Integer parentOrder;
}

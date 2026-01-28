package com.team.catchup.jira.entity;

import com.team.catchup.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentlyReadIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private IssueMetadata issueMetadata;

    private LocalDateTime lastViewedAt;

    public void updateLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }

    public static RecentlyReadIssue of(Member member, IssueMetadata issueMetadata) {
        return RecentlyReadIssue.builder()
                .member(member)
                .issueMetadata(issueMetadata)
                .lastViewedAt(LocalDateTime.now())
                .build();

    }

    public void replaceIssue(IssueMetadata newIssueMetadata) {
        this.issueMetadata = newIssueMetadata;
        this.lastViewedAt = LocalDateTime.now();
    }
}

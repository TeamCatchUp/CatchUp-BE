package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "issue_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueLink {

    @Id
    @Column(name = "link_id")
    private Integer linkId;

    // Outward Issue | ex) blocks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outward_issue_id")
    private IssueMetadata outwardIssue;

    // Inward Issue | ex) is Blocked By
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inward_issue_id")
    private IssueMetadata inwardIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_type_id", nullable = false)
    private IssueLinkType linkType;

    @Builder
    public IssueLink(Integer linkId, IssueMetadata outwardIssue, IssueMetadata inwardIssue, IssueLinkType linkType) {
        this.linkId = linkId;
        this.outwardIssue = outwardIssue;
        this.inwardIssue = inwardIssue;
        this.linkType = linkType;
    }

}

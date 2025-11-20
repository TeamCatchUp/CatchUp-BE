package com.team.catchup.jira.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "outward_issue_id")
    private Integer outwardIssueId;

    // Inward Issue | ex) is Blocked By
    @Column(name = "inward_issue_id")
    private Integer inwardIssueId;

    @Column(name = "link_type_id", nullable = false)
    private Integer linkTypeId;

    @Builder
    public IssueLink(Integer linkId, Integer outwardIssueId, Integer inwardIssueId, Integer linkTypeId) {
        this.linkId = linkId;
        this.outwardIssueId = outwardIssueId;
        this.inwardIssueId = inwardIssueId;
        this.linkTypeId = linkTypeId;
    }

}

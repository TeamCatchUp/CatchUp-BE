package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueMetadata {

    @Id
    @Column(name = "issue_id", nullable = false, unique = true)
    private Integer issueId;

    @Column(name = "issue_key", nullable = false, unique = true)
    private String issueKey;

    @Column(name = "issue_url")
    private String self;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_type_id")
    private IssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private JiraProject project;

    @Column(name = "summary")
    private String summary;

    @Column(name = "parent_issue_id")
    private Integer parentIssueId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "priority_id")
    private Integer priorityId;

    @Column(name = "duedate")
    private LocalDateTime duedate;

    @Column(name = "created_at")
    private LocalDateTime issueCreatedAt;

    // 실제 종료일을 담고 있지 않고, 종료의 이유를 담고 있습니다.
    @Column(name = "resolution_id")
    private Integer resolutionId;

    // 해당 이슈의 종료일
    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private JiraUser creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private JiraUser reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private JiraUser assignee;

    @OneToMany(mappedBy = "issueId", fetch = FetchType.LAZY)
    private List<IssueAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "inwardIssue", fetch = FetchType.LAZY)
    private List<IssueLink> inwardLinks = new ArrayList<>();

    @OneToMany(mappedBy = "outwardIssue", fetch = FetchType.LAZY)
    private List<IssueLink> outwardLinks = new ArrayList<>();

    @Builder
    public IssueMetadata(Integer issueId, String issueKey, String self,
                         IssueType issueType, JiraProject project,
                         String summary,
                         Integer parentIssueId, Integer statusId, Integer priorityId,
                         LocalDateTime duedate, LocalDateTime issueCreatedAt,
                         Integer resolutionId, LocalDateTime resolutionDate,
                         JiraUser creator, JiraUser reporter, JiraUser assignee) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.self = self;
        this.issueType = issueType;
        this.project = project;
        this.summary = summary;
        this.parentIssueId = parentIssueId;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.duedate = duedate;
        this.issueCreatedAt = issueCreatedAt;
        this.resolutionId = resolutionId;
        this.resolutionDate = resolutionDate;
        this.creator = creator;
        this.reporter = reporter;
        this.assignee = assignee;
    }
}

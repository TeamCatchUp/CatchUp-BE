package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_id", nullable = false, unique = true)
    private Integer issueId;

    @Column(name = "issue_key", nullable = false, unique = true)
    private String issueKey;

    @Column(name = "issue_type_id", nullable = false)
    private Integer issueTypeId;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "summary")
    private String summary;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

    @Column(name = "resolution_id")
    private Integer resolutionId;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "reporter_id")
    private String reporterId;

    @Column(name = "assignee_id")
    private String assigneeId;

    @Builder
    public IssueMetadata(Integer issueId, String issueKey, Integer issueTypeId,
                         Integer projectId, String summary, String description,
                         Integer parentIssueId, Integer statusId, Integer priorityId,
                         LocalDateTime duedate, LocalDateTime issueCreatedAt,
                         Integer resolutionId, LocalDateTime resolutionDate,
                         String creatorId, String reporterId, String assigneeId) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.issueTypeId = issueTypeId;
        this.projectId = projectId;
        this.summary = summary;
        this.description = description;
        this.parentIssueId = parentIssueId;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.duedate = duedate;
        this.issueCreatedAt = issueCreatedAt;
        this.resolutionId = resolutionId;
        this.resolutionDate = resolutionDate;
        this.creatorId = creatorId;
        this.reporterId = reporterId;
        this.assigneeId = assigneeId;
    }
}

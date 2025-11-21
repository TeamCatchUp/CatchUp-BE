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
@Table(name = "jira_project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JiraProject {

    @Id
    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "project_key", nullable = false, unique = true, length = 50)
    private String projectKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // "software", "business" ...
    @Column(name = "project_type_key", length = 50)
    private String projectTypeKey;

    // "next-gen", "classic" ...
    @Column(name = "style", length = 20)
    private String style;

    @Column(name = "is_simplified")
    private Boolean simplified;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "totalIssueCount")
    private Integer totalIssueCount;

    @Column(name = "last_update_time")
    private LocalDateTime lastIssueUpdateTime;

    @OneToMany(mappedBy = "project",  fetch = FetchType.LAZY)
    private List<IssueMetadata> issues = new ArrayList<>();

    @Builder
    public JiraProject(Integer projectId, String projectKey, String name, String description,
                   String projectTypeKey, String style, Boolean simplified,
                   Boolean isPrivate, String avatarUrl,  Integer totalIssueCount, LocalDateTime lastIssueUpdateTime) {
        this.projectId = projectId;
        this.projectKey = projectKey;
        this.name = name;
        this.description = description;
        this.projectTypeKey = projectTypeKey;
        this.style = style;
        this.simplified = simplified;
        this.isPrivate = isPrivate;
        this.avatarUrl = avatarUrl;
        this.totalIssueCount = totalIssueCount;
        this.lastIssueUpdateTime = lastIssueUpdateTime;
    }
}


package com.team.catchup.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueMetaDataResponse (
        List<JiraIssue> issues,

        @JsonProperty("nextPageToken")
        String nextPageToken,

        @JsonProperty("isLast")
        Boolean isLast
){
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraIssue(
            String id,
            String key,
            Fields fields
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fields(
            @JsonProperty("issuetype")
            IssueType issueType,

            @JsonProperty("parent")
            ParentIssue parentIssue,

            @JsonProperty("project")
            Project project,

            @JsonProperty("description")
            String description,

            @JsonProperty("statusCategory")
            StatusCategory statusCategory,

            @JsonProperty("resolution")
            IssueResolution issueResolution,

            @JsonProperty("resolutiondate")
            String resolutionDate,

            @JsonProperty("summary")
            String summary,

            @JsonProperty("creator")
            UserRecord creator,

            @JsonProperty("created")
            String issueCreatedAt,

            @JsonProperty("reporter")
            UserRecord reporter,

            @JsonProperty("priority")
            IssuePriority issuePriority,

            @JsonProperty("duedate")
            String issueDueDate,

            @JsonProperty("assignee")
            UserRecord assignee
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueType(
            @JsonProperty("id")
            String id,

            @JsonProperty("iconUrl")
            String issueTypeIconUrl,

            @JsonProperty("name")
            String issueTypeName,

            @JsonProperty("subtask")
            Boolean isSubTask
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParentIssue(
            @JsonProperty("id")
            String parentIssueId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(
            @JsonProperty("id")
            String projectId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusCategory(
            @JsonProperty("id")
            String statusId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueResolution(
            @JsonProperty("id")
            String resolutionId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserRecord(
            @JsonProperty("accountId")
            String id
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssuePriority(
            @JsonProperty("id")
            String priorityId
    ) {}
}
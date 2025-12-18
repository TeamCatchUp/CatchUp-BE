package com.team.catchup.jira.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueMetadataApiResponse(
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
            String self,
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

            @JsonProperty("statusCategory")
            StatusCategory statusCategory,

            @JsonProperty("resolution")
            IssueResolution issueResolution,

            @JsonProperty("resolutiondate")
            String resolutionDate,

            @JsonProperty("summary")
            String summary,

            @JsonProperty("creator")
            UserID creator,

            @JsonProperty("created")
            String issueCreatedAt,

            @JsonProperty("reporter")
            UserID reporter,

            @JsonProperty("priority")
            IssuePriority issuePriority,

            @JsonProperty("duedate")
            String issueDueDate,

            @JsonProperty("assignee")
            UserID assignee,

            @JsonProperty("issuelinks")
            List<IssueLink> issueLinks,

            @JsonProperty("attachment")
            List<IssueAttachment> attachments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueLink(
            @JsonProperty("id")
            String id,

            @JsonProperty("type")
            LinkType type,

            @JsonProperty("inwardIssue")
            LinkedIssue inwardIssue,

            @JsonProperty("outwardIssue")
            LinkedIssue outwardIssue
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LinkType(
            @JsonProperty("id")
            String id,

            @JsonProperty("name")
            String name,

            @JsonProperty("inward")
            String inward,

            @JsonProperty("outward")
            String outward,

            @JsonProperty("self")
            String self
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LinkedIssue(
            @JsonProperty("id")
            String id,

            @JsonProperty("key")
            String key
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueType(
            @JsonProperty("id")
            String id
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
    public record UserID(
            @JsonProperty("accountId")
            String id
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssuePriority(
            @JsonProperty("id")
            String priorityId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueAttachment(
            @JsonProperty("id")
            String id,

            @JsonProperty("filename")
            String filename,

            @JsonProperty("author")
            UserID author,

            @JsonProperty("created")
            String created,

            @JsonProperty("size")
            String size,

            @JsonProperty("mimeType")
            String mimetype,

            @JsonProperty("content")
            String content,

            @JsonProperty("thumbnail")
            String thumbnail
    ) {}
}
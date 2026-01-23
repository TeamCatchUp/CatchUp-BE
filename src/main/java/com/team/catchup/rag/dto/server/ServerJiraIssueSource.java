package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@RequiredArgsConstructor
public class ServerJiraIssueSource extends ServerSource {

    @JsonProperty("issue_type_name")
    private String issueTypeName;

    private String summary;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("issue_key")
    private String issueKey;

    @JsonProperty("parent_key")
    private String parentKey;

    @JsonProperty("parent_summary")
    private String parentSummary;

    @JsonProperty("status_id")
    private Integer statusId;

    @JsonProperty("assignee_name")
    private String assigneeName;

}
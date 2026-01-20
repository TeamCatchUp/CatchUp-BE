package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.ServerJiraIssueSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@RequiredArgsConstructor
public class ClientJiraIssueSource extends ClientSource {

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

    public static ClientJiraIssueSource from(ServerJiraIssueSource source) {
        return ClientJiraIssueSource.builder()
                // 공통 필드
                .index(source.getIndex())
                .isCited(source.getIsCited())
                .sourceType(source.getSourceType())
                .relevanceScore(source.getRelevanceScore())
                .htmlUrl(source.getHtmlUrl())
                .content(source.getText())
                .owner(source.getOwner())
                .repo(source.getRepo())
                // Jira Issue 전용
                .issueTypeName(source.getIssueTypeName())
                .summary(source.getSummary())
                .projectName(source.getProjectName())
                .issueKey(source.getIssueKey())
                .parentKey(source.getParentKey())
                .parentSummary(source.getParentSummary())
                .statusId(source.getStatusId())
                .assigneeName(source.getAssigneeName())
                .build();
    }
}

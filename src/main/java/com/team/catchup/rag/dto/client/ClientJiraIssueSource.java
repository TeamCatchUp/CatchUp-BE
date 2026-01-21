package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.ServerJiraIssueSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@RequiredArgsConstructor
public class ClientJiraIssueSource extends ClientSource {

    private String issueTypeName;

    private String summary;

    private String projectName;

    private String issueKey;

    private String parentKey;

    private String parentSummary;

    private Integer statusId;

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

package com.team.catchup.meilisearch.document;

import com.team.catchup.jira.entity.IssueMetadata;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Jira 이슈 데이터를 저장하는 Document
 */
@Getter
@Setter
@Builder
public class JiraIssueDocument implements MeiliSearchDocument {
    private String id; // 예) BJDD-72
    private String selfUrl;

    private String summary;
    private String description;

    private String projectName;
    private String issueTypeName;
    private String assigneeName;
    private String reporterName;

    private Integer statusId;
    private Integer priorityId;
    private String createdAt;
    private String resolutionDate;

    private String projectKey;

    private String parentKey;
    private String parentSummary;

    @Override
    public String getIndexName() {
        String safeProjectName = (this.projectKey != null ? this.projectKey : "default")
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9-_]", "_");
        return safeProjectName + "_jira_issue";
    }

    @Override
    public String getPrimaryKeyFieldName() {
        return "id";
    }


    public static JiraIssueDocument from(IssueMetadata entity, IssueMetadata parentEntity) {
        if (entity == null) return null;

        JiraIssueDocumentBuilder builder = JiraIssueDocument.builder()
                .id(entity.getIssueKey())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .selfUrl(entity.getSelf())
                .statusId(entity.getStatusId())
                .priorityId(entity.getPriorityId());

        if (entity.getIssueCreatedAt() != null) builder.createdAt(entity.getIssueCreatedAt().toString());
        if (entity.getResolutionDate() != null) builder.resolutionDate(entity.getResolutionDate().toString());

        // 연관 객체 데이터 Flattening
        if (entity.getProject() != null) builder.projectName(entity.getProject().getName());
        if (entity.getIssueType() != null) {
            builder.projectKey(entity.getProject().getProjectKey());
            builder.issueTypeName(entity.getIssueType().getName());
        }
        if (entity.getAssignee() != null) builder.assigneeName(entity.getAssignee().getDisplayName());
        if (entity.getReporter() != null) builder.reporterName(entity.getReporter().getDisplayName());

        if (parentEntity != null) {
            builder.parentKey(parentEntity.getIssueKey());
            builder.parentSummary(parentEntity.getSummary());
        }

        return builder.build();
    }
}

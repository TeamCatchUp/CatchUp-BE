package com.team.catchup.meilisearch.document;

import com.team.catchup.jira.entity.IssueMetadata;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Jira 이슈 데이터를 저장하는 Document
 * Python 컨슈머를 위해 필드명을 snake_case로 정의함
 */
@Getter
@Setter
@Builder
public class JiraIssueDocument implements MeiliSearchDocument {
    private String id;
    private String self_url;

    private String summary;
    private String description;

    private String project_name;
    private String issue_type_name;
    private String assignee_name;
    private String reporter_name;

    private Integer status_id;
    private Integer priority_id;
    private String created_at;
    private String resolution_date;

    private String project_key;

    private String parent_key;
    private String parent_summary;

    private Integer source_type;

    @Override
    public String getIndexName() {
        String safeProjectName = (this.project_key != null ? this.project_key : "default")
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
                .self_url(entity.getSelf())
                .status_id(entity.getStatusId())
                .priority_id(entity.getPriorityId())
                .source_type(3);

        if (entity.getIssueCreatedAt() != null) builder.created_at(entity.getIssueCreatedAt().toString());
        if (entity.getResolutionDate() != null) builder.resolution_date(entity.getResolutionDate().toString());

        if (entity.getProject() != null) builder.project_name(entity.getProject().getName());
        if (entity.getIssueType() != null) {
            builder.project_key(entity.getProject().getProjectKey());
            builder.issue_type_name(entity.getIssueType().getName());
        }
        if (entity.getAssignee() != null) builder.assignee_name(entity.getAssignee().getDisplayName());
        if (entity.getReporter() != null) builder.reporter_name(entity.getReporter().getDisplayName());

        if (parentEntity != null) {
            builder.parent_key(parentEntity.getIssueKey());
            builder.parent_summary(parentEntity.getSummary());
        }

        return builder.build();
    }
}
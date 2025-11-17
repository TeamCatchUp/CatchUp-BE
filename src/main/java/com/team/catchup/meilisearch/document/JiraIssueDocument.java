package com.team.catchup.meilisearch.document;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Jira 이슈 데이터를 저장하는 Document
 */
@Getter
@Setter
public class JiraIssueDocument implements MeiliSearchDocument {
    private String id; // 예) BJDD-72

    private String summary;
    private String description;

    private String createdAt;
    private String resolutionDate;

    private String assigneeId;
    private String creatorId;
    private String reporterId;


    /**
     * Provide the MeiliSearch index name used for Jira issue documents.
     *
     * @return the index name "jira_issue"
     */
    @Override
    public String getIndexName() {
        return "jira_issue";
    }

    /**
     * Primary key field name for this document.
     *
     * @return the primary key field name `"id"`.
     */
    @Override
    public String getPrimaryKeyFieldName() {
        return "id";
    }


    /**
     * Create a JiraIssueDocument from a Jira API issue response.
     *
     * Maps relevant fields (id, summary, description, createdAt, resolutionDate,
     * assigneeId, creatorId, reporterId) from the provided IssueMetaDataResponse.JiraIssue.
     *
     * @param jiraIssue the Jira API issue response to convert; may be null
     * @return a JiraIssueDocument populated from the response, or `null` if {@code jiraIssue} is null
     */
    static public JiraIssueDocument from(IssueMetaDataResponse.JiraIssue jiraIssue) {
        if (jiraIssue == null){
            return null;
        }

        JiraIssueDocument jiraIssueDocument = new JiraIssueDocument();

        // 예) BJDD-72
        jiraIssueDocument.setId(jiraIssue.key());

        if (jiraIssue.fields() == null){
            // fields가 없으면 최소한의 정보만 반환
            return jiraIssueDocument;
        }

        IssueMetaDataResponse.Fields fields = jiraIssue.fields();

        jiraIssueDocument.setSummary(fields.summary());
        jiraIssueDocument.setDescription(fields.description());
        jiraIssueDocument.setCreatedAt(fields.issueCreatedAt());
        jiraIssueDocument.setResolutionDate(fields.resolutionDate());

        // Null check
        jiraIssueDocument.setAssigneeId(
                fields.assignee() != null ? fields.assignee().id() : null
        );
        jiraIssueDocument.setCreatorId(
                fields.creator() != null ? fields.creator().id() : null
        );
        jiraIssueDocument.setReporterId(
                fields.reporter() != null ? fields.reporter().id() : null
        );

        return jiraIssueDocument;
    }
}
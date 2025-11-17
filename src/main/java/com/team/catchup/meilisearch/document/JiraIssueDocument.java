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


    @Override
    public String getIndexName() {
        return "jira_issue";
    }

    @Override
    public String getPrimaryKeyFieldName() {
        return "id";
    }


    /**
     * Jira API 응답을 가공해서 JiraIssueDocument로 변환
     * @param jiraIssue IssueMetaDataResponse.JiraIssue
     * @return JiraIssueDocument 객체
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

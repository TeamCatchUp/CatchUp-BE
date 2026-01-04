package com.team.catchup.meilisearch.document;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
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
     * @param jiraIssue IssueMetadataApiResponse.JiraIssue
     * @return JiraIssueDocument 객체
     */
    public static JiraIssueDocument from(IssueMetadataApiResponse.JiraIssue jiraIssue) {
        if (jiraIssue == null){
            return null;
        }

        JiraIssueDocumentBuilder builder = JiraIssueDocument.builder();

        // 예) BJDD-72
        builder.id(jiraIssue.key());

        if (jiraIssue.fields() == null){
            // fields가 없으면 최소한의 정보만 반환
            return builder.build();
        }

        IssueMetadataApiResponse.Fields fields = jiraIssue.fields();

        builder.summary(fields.summary());
        builder.createdAt(fields.issueCreatedAt());
        builder.resolutionDate(fields.resolutionDate());

        // Description 추출
        if (fields.description() != null) {
            builder.description(fields.description().getAllText());
        }

        // Null check
        builder.assigneeId(
                fields.assignee() != null ? fields.assignee().id() : null
        );
        builder.creatorId(
                fields.creator() != null ? fields.creator().id() : null
        );
        builder.reporterId(
                fields.reporter() != null ? fields.reporter().id() : null
        );

        return builder.build();
    }
}

package com.team.catchup.meilisearch.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import lombok.*;

/**
 * Jira 이슈 데이터를 저장하는 Document
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueDocument implements MeiliSearchDocument {
    private String id; // 예) BJDD-72

    @JsonIgnore
    private String projectKey; // 예) BJDD

    private String summary;
    private String description;

    private String createdAt;
    private String resolutionDate;

    private String assigneeId;
    private String creatorId;
    private String reporterId;


    @Override
    @JsonIgnore
    public String getIndexName() {
        return "jira_issue_" + projectKey;
    }

    @Override
    @JsonIgnore
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

        String projectKey = parseProjectKey(jiraIssue.key());

        IssueMetaDataResponse.Fields fields = jiraIssue.fields();
        if (fields == null){
            return JiraIssueDocument.builder()
                    .id(jiraIssue.key())
                    .projectKey(projectKey)
                    .build();
        }

        return JiraIssueDocument.builder()
                .id(jiraIssue.key()) // BJDD-72
                .projectKey(projectKey) // BJDD
                .summary(fields.summary())
                .description(fields.description() != null ? fields.description().getAllText() : "")
                .createdAt(fields.issueCreatedAt())
                .resolutionDate(fields.resolutionDate())
                .assigneeId(extractUserId(fields.assignee()))
                .creatorId(extractUserId(fields.creator()))
                .reporterId(extractUserId(fields.reporter()))
                .build();
    }

    /**
     * issue key를 입력 받아 project key를 추출한다. issueKey가 유효하지 않은 경우 default를 반환한다.
     * @param issueKey 예: BJDD-72
     * @return 예: BJDD
     */
    private static String parseProjectKey(String issueKey){
        if (issueKey != null && issueKey.contains("-")) {
            return issueKey.split("-")[0];
        }
        return "default";
    }

    /**
     * IssuMetaDataResponse.UserID 객체를 받아 null check 후 id를 반환한다.
     * @param userId UserID 객체
     * @return (nullable) Jira account id
     */
    private static String extractUserId(IssueMetaDataResponse.UserID userId){
        return userId != null ? userId.id() : null;
    }
}

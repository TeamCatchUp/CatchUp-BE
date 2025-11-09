package com.team.catchup.meilisearch.document;

import lombok.Getter;
import lombok.Setter;

/**
 * Jira 이슈 데이터를 저장하는 Document
 */
@Getter
@Setter
public class JiraIssueDocument implements MeiliSearchDocument {
    private String id;

    private String summary;
    private String description;

    @Override
    public String getIndexName() {
        return "jira_issue";
    }

    @Override
    public String getPrimaryKeyFieldName() {
        return "id";
    }
}

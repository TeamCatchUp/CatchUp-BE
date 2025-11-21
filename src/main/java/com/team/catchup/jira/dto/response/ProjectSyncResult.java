package com.team.catchup.jira.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectSyncResult {

    private final String projectKey;
    private final boolean success;
    private final String errorMessage;

    private final SyncCount issues;
    private final SyncCount issueLinks;
    private final SyncCount attachments;

    public static ProjectSyncResult success(String projectKey, SyncCount issues,
                                            SyncCount issueLinks, SyncCount attachments) {
        return ProjectSyncResult.builder()
                .projectKey(projectKey)
                .success(true)
                .issues(issues)
                .issueLinks(issueLinks)
                .attachments(attachments)
                .build();
    }

    public static ProjectSyncResult failure(String projectKey, String errorMessage) {
        return ProjectSyncResult.builder()
                .projectKey(projectKey)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}

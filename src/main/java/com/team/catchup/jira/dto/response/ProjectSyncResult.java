package com.team.catchup.jira.dto.response;

import com.team.catchup.common.sse.dto.SyncCount;

public record ProjectSyncResult(
        String projectKey,
        boolean success,
        String errorMessage,
        SyncCount issues,
        SyncCount issueLinks,
        SyncCount attachments
) {
    public static ProjectSyncResult success(
            String projectKey,
            SyncCount issues,
            SyncCount issueLinks,
            SyncCount attachments
    ) {
        return new ProjectSyncResult(
                projectKey,
                true,
                null,
                issues,
                issueLinks,
                attachments
        );
    }

    public static ProjectSyncResult failure(String projectKey, String errorMessage) {
        return new ProjectSyncResult(
                projectKey,
                false,
                errorMessage,
                null,
                null,
                null
        );
    }
}
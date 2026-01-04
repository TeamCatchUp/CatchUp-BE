package com.team.catchup.jira.dto.response;

import com.team.catchup.common.sse.dto.SyncCount;
import com.team.catchup.jira.dto.JiraSyncStep;

public record JiraSyncProgress(
        JiraSyncStep step,
        SyncCount count,
        ProjectSyncResult projectResult,
        String currentProjectKey,
        String message
) {
    public static JiraSyncProgress of(
            JiraSyncStep step,
            SyncCount count,
            String message
    ) {
        return new JiraSyncProgress(step, count, null, null, message);
    }

    public static JiraSyncProgress ofProjectIssue(
            JiraSyncStep step,
            ProjectSyncResult result,
            String currentProjectKey,
            String message
    ) {
        return new JiraSyncProgress(step, null, result, currentProjectKey, message);
    }
}
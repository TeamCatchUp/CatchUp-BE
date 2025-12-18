package com.team.catchup.common.sse.dto;

import com.team.catchup.jira.dto.JiraSyncStep;
import com.team.catchup.jira.dto.response.ProjectSyncResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JiraSyncProgress {
    private JiraSyncStep step;
    private SyncCount count;
    private ProjectSyncResult result;
    private String currentProjectKey;
    private String message;

    public static JiraSyncProgress of (JiraSyncStep step, SyncCount count, String message) {
        return JiraSyncProgress.builder()
                .step(step)
                .count(count)
                .message(message)
                .build();
    }

    public static JiraSyncProgress ofProjectIssue (JiraSyncStep step, ProjectSyncResult result, String currentProjectKey, String message) {
        return JiraSyncProgress.builder()
                .step(step)
                .result(result)
                .currentProjectKey(currentProjectKey)
                .message(message)
                .build();
    }
}

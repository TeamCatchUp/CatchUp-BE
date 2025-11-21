package com.team.catchup.jira.dto.response;

import com.team.catchup.jira.dto.SyncStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class FullSyncResult {

    private final SyncStep lastCompletedStep;
    private final SyncStep failedStep;
    private final String errorMessage;

    private final SyncCount projects;
    private final SyncCount users;
    private final SyncCount issuesTypes;

    private final Map<String, ProjectSyncResult> projectSyncResults;
    private final List<String> failedProjectKeys;
}

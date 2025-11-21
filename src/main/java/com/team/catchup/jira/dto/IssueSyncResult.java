package com.team.catchup.jira.dto;

import com.team.catchup.jira.dto.response.SyncCount;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssueSyncResult {

    private final SyncCount issues;
    private final SyncCount issueLinks;
    private final SyncCount attachments;
}

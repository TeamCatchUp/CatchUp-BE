package com.team.catchup.jira.dto.response;


import com.team.catchup.common.sse.dto.SyncCount;

public record IssueSyncResult(
        SyncCount issues,
        SyncCount issueLinks,
        SyncCount attachments
) {
    public static IssueSyncResult of(
            SyncCount issues,
            SyncCount issueLinks,
            SyncCount attachments
    ) {
        return new IssueSyncResult(issues, issueLinks, attachments);
    }
}
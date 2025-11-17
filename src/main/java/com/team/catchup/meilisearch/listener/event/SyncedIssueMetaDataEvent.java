package com.team.catchup.meilisearch.listener.event;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;

public record SyncedIssueMetaDataEvent(
        IssueMetaDataResponse syncedIssueMetaDataResponse
){}

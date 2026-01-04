package com.team.catchup.meilisearch.listener.event;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;

public record SyncedIssueMetaDataEvent(
        IssueMetadataApiResponse syncedIssueMetaDataResponse
){}

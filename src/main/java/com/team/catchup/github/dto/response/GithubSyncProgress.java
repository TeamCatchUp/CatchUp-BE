package com.team.catchup.github.dto.response;

import com.team.catchup.github.dto.GithubSyncStep;

public record GithubSyncProgress(
    GithubSyncStep step,
    SyncCount count,
    String repositoryName,
    String message
) {
    public static GithubSyncProgress of(GithubSyncStep step, SyncCount count, String repositoryName, String message) {
        return new GithubSyncProgress(step, count, repositoryName, message);
    }
}

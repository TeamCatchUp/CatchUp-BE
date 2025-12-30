package com.team.catchup.github.dto;

public enum GithubSyncStep {
    REPOSITORY_INFO,
    COMMITS,
    PULL_REQUESTS,
    ISSUES,
    COMMENTS,
    REVIEWS,
    FILE_CHANGES,
    COMPLETED
}

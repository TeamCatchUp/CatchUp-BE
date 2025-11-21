package com.team.catchup.jira.dto;

public enum SyncStep {
    PROJECTS,
    USERS,
    ISSUE_TYPES,
    PROJECT_ISSUES, // Issue MetaData + Issue Link + Attachment
    COMPLETED
}

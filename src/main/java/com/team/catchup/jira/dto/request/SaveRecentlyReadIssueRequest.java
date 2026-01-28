package com.team.catchup.jira.dto.request;

import jakarta.validation.constraints.NotNull;

public record SaveRecentlyReadIssueRequest(
        @NotNull Integer issueId
) {
}
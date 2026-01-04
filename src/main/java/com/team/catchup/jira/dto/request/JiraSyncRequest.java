package com.team.catchup.jira.dto.request;

import com.team.catchup.jira.dto.JiraSyncStep;

import java.util.List;

public record JiraSyncRequest(
        JiraSyncStep startFrom,
        List<String> projectKeys
) {
    public static JiraSyncRequest fullSync() {
        return new JiraSyncRequest(JiraSyncStep.PROJECTS, null);
    }

    public static JiraSyncRequest retryFrom(
            JiraSyncStep startFrom,
            List<String> projectKeys
    ) {
        return new JiraSyncRequest(startFrom, projectKeys);
    }
}

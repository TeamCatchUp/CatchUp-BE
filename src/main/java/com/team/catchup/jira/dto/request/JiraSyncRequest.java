package com.team.catchup.jira.dto.request;

import com.team.catchup.jira.dto.JiraSyncStep;

import java.util.List;

public record JiraSyncRequest(
        Long userId,
        JiraSyncStep startFrom,
        List<String> projectKeys
) {
    public static JiraSyncRequest fullSync(Long userId) {
        return new JiraSyncRequest(userId, JiraSyncStep.PROJECTS, null);
    }

    public static JiraSyncRequest retryFrom(
            Long userId,
            JiraSyncStep startFrom,
            List<String> projectKeys
    ) {
        return new JiraSyncRequest(userId, startFrom, projectKeys);
    }
}

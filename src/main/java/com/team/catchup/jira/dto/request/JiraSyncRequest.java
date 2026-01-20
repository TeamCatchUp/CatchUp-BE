package com.team.catchup.jira.dto.request;

import com.team.catchup.jira.dto.JiraSyncStep;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record JiraSyncRequest(
        @NotNull(message = "Start Step is Required")
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

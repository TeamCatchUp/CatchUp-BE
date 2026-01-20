package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.client.UserSelectedPullRequest;

import java.util.List;
import java.util.UUID;

public record ServerChatResumeRequest(
        @JsonProperty("session_id")
        UUID sessionId,

        @JsonProperty("user_selected_pull_requests")
        List<UserSelectedPullRequest> userSelectedPullRequests
) {
    public static ServerChatResumeRequest of(
            UUID sessionId, List<UserSelectedPullRequest> userSelectedPullRequests
    ) {
        return new ServerChatResumeRequest(
                sessionId,
                userSelectedPullRequests
        );
    }
}

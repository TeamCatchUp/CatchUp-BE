package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.PullRequestCandidate;

import java.util.List;
import java.util.UUID;

public record ClientChatResumeRequest(
        UUID sessionID,
        List<PullRequestCandidate> userSelectedPullRequests
) {
}

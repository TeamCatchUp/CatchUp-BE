package com.team.catchup.rag.dto.client;

import java.util.List;
import java.util.UUID;

public record ClientChatResumeRequest(
        UUID sessionId,
        List<UserSelectedPullRequest> userSelectedPullRequests
) {
}

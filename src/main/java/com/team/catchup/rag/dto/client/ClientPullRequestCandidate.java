package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.PullRequestCandidate;
import lombok.Builder;

@Builder
public record ClientPullRequestCandidate(
        int prNumber,
        String title,
        String repoName,
        String summary,
        String owner,
        Integer createdAt
) {
    public static ClientPullRequestCandidate from(PullRequestCandidate candidate) {
        return ClientPullRequestCandidate.builder()
                .prNumber(candidate.prNumber())
                .title(candidate.title())
                .repoName(candidate.repoName())
                .summary(candidate.summary())
                .owner(candidate.owner())
                .createdAt(candidate.createdAt())
                .build();
    }
}

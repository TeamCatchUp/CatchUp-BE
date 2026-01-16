package com.team.catchup.rag.dto.server;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 사용자 선택용 정보
 */
@Builder
public record PullRequestCandidate(
        @NotBlank String pr_number,
        @NotBlank String title,
        @NotBlank String repo_name,
        String summary,
        @NotBlank String owner
) {
    public static PullRequestCandidate createServerResponse(PullRequestCandidate candidate) {
        return PullRequestCandidate.builder()
                .pr_number(candidate.pr_number)
                .repo_name(candidate.repo_name)
                .owner(candidate.owner)
                .build();
    }
}
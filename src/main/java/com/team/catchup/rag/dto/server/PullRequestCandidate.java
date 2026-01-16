package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 사용자 선택용 정보
 */
@Builder
public record PullRequestCandidate(
        @JsonProperty("pr_number")
        @NotNull int prNumber,
        @NotBlank String title,
        @JsonProperty("repo_name")
        @NotBlank String repoName,
        String summary,
        @NotBlank String owner
) {
}
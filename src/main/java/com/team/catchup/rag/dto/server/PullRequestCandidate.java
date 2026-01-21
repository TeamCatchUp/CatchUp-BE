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

        @NotNull
        @JsonProperty("pr_number")
        int prNumber,

        @NotBlank String title,

        @JsonProperty("repo")
        @NotBlank String repoName,

        String summary,

        @NotBlank
        String owner,

        @JsonProperty("created_at")
        Integer createdAt
) {
}
package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSelectedPullRequest(
        @JsonAlias({"prNumber"})
        @JsonProperty("pr_number")
        int prNumber,

        @JsonAlias({"repoName"})
        @JsonProperty("repo")
        String repoName,

        String owner
) {
}

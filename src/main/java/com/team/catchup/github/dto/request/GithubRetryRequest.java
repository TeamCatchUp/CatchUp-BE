package com.team.catchup.github.dto.request;

import com.team.catchup.github.dto.GithubSyncStep;
import jakarta.validation.constraints.NotBlank;

public record GithubRetryRequest(
        @NotBlank(message = "Repository Owner is Required")
        String owner,

        @NotBlank(message = "Repository Name is Required")
        String repository,

        @NotBlank(message = "Target Branch Name is Required")
        String branch,

        @NotBlank(message = "Start Point is Required")
        GithubSyncStep startFrom
) {
}

package com.team.catchup.github.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GithubFullSyncRequest(
        @NotBlank(message = "Repository Owner is Required")
        String owner,

        @NotBlank(message = "Repository Name is Required")
        String repository,

        @NotBlank(message = "Target Branch Name is Required")
        String branch
) {
}

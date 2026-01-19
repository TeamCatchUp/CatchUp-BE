package com.team.catchup.github.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPrIssuePayload {
    private String action;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;

    private Issue issue;
    private Repository repository;

    @Getter
    @NoArgsConstructor
    public static class PullRequest {
        private Integer number;
        private String title;
        private String state;
    }

    @Getter
    @NoArgsConstructor
    public static class Issue {
        private Integer number;
        private String title;
    }

    @Getter
    @NoArgsConstructor
    public static class Repository {
        private Long id;
        private String name;
        private Owner owner;
    }

    @Getter
    @NoArgsConstructor
    public static class Owner {
        private String login;
    }
}
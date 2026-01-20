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
        private Long id;
        private Integer number;
        private String title;
        private String state;
        private User user;
        private Base base;
        private Head head;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("closed_at")
        private String closedAt;

        @JsonProperty("merged_at")
        private String mergedAt;

        @JsonProperty("merge_commit_sha")
        private String mergeCommitSha;

        @JsonProperty("html_url")
        private String htmlUrl;

        private Boolean merged;
    }

    @Getter
    @NoArgsConstructor
    public static class Issue {
        private Long id;
        private Integer number;
        private String title;
        private String state;
        private User user;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("closed_at")
        private String closedAt;

        @JsonProperty("html_url")
        private String htmlUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class User {
        private String login;
    }

    @Getter
    @NoArgsConstructor
    public static class Base {
        private String ref;
    }

    @Getter
    @NoArgsConstructor
    public static class Head {
        private String ref;
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
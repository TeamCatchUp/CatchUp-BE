package com.team.catchup.github.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPushPayload {
    private String ref;
    private Repository repository;
    private List<Commit> commits;

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

    @Getter
    @NoArgsConstructor
    public static class Commit {
        private String id;
        private List<String> added;
        private List<String> modified;
        private List<String> removed;
    }
}

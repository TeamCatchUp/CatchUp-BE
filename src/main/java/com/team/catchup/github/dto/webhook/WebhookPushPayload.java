package com.team.catchup.github.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPushPayload {
    private String ref;  // refs/heads/main
    private Repository repository;
    private List<Commit> commits;

    /**
     * ref에서 브랜치 이름 추출
     * @return 브랜치 이름 (예: "main", "feature/test")
     */
    public String getBranchName() {
        if (ref == null) {
            return null;
        }
        // "refs/heads/main" -> "main"
        if (ref.startsWith("refs/heads/")) {
            return ref.substring("refs/heads/".length());
        }
        return ref;
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

    @Getter
    @NoArgsConstructor
    public static class Commit {
        private String id;
        private String message;
        private String timestamp;
        private String url;
        private Author author;
        private List<String> added;
        private List<String> modified;
        private List<String> removed;
    }

    @Getter
    @NoArgsConstructor
    public static class Author {
        private String name;
        private String email;
    }
}

package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.entity.GithubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GithubCommitMapper {

    private final ObjectMapper objectMapper;
    private final GithubCommitParentMapper commitParentMapper;

    public GithubCommit toEntity(JsonNode apiResponse, GithubRepository repository) {
        try {
            JsonNode commit = apiResponse.get("commit");
            JsonNode author = commit.get("author");

            String message = commit.get("message").asText();
            String shortMessage = message.split("\n")[0];
            if (shortMessage.length() > 500) {
                shortMessage = shortMessage.substring(0, 500);
            }

            // Build commit entity first (without parents to avoid circular dependency)
            GithubCommit githubCommit = GithubCommit.builder()
                    .repository(repository)
                    .sha(apiResponse.get("sha").asText())
                    .message(shortMessage)
                    .authorName(getTextOrNull(author, "name"))
                    .authorEmail(getTextOrNull(author, "email"))
                    .authorDate(parseDateTime(getTextOrNull(author, "date")))
                    .additions(getIntOrNull(apiResponse.get("stats"), "additions"))
                    .deletions(getIntOrNull(apiResponse.get("stats"), "deletions"))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();

            // Map parent commits after building the main entity
            List<String> parentShas = extractParentShas(apiResponse);
            if (!parentShas.isEmpty()) {
                githubCommit.setParents(commitParentMapper.toEntities(parentShas, githubCommit));
            }

            return githubCommit;
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map commit", e);
            throw new RuntimeException("Failed to map commit", e);
        }
    }

    public List<String> extractParentShas(JsonNode apiResponse) {
        List<String> parentShas = new ArrayList<>();
        JsonNode parents = apiResponse.get("parents");
        if (parents != null && parents.isArray()) {
            parents.forEach(parent -> {
                if (parent.has("sha")) {
                    parentShas.add(parent.get("sha").asText());
                }
            });
        }
        return parentShas;
    }

    private String getTextOrNull(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private Integer getIntOrNull(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asInt() : null;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTime).toLocalDateTime();
    }
}

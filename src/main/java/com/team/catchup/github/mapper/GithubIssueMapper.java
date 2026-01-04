package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubIssue;
import com.team.catchup.github.entity.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class GithubIssueMapper {

    public GithubIssue toEntity(JsonNode apiResponse, GithubRepository repository) {
        try {
            return GithubIssue.builder()
                    .issueId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .number(apiResponse.get("number").asInt())
                    .title(apiResponse.get("title").asText())
                    .status(parseStatus(apiResponse.get("state").asText()))
                    .authorLogin(apiResponse.get("user").get("login").asText())
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .closedAt(parseDateTime(getTextOrNull(apiResponse, "closed_at")))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map issue", e);
            throw new RuntimeException("Failed to map issue", e);
        }
    }

    private GithubIssue.IssueStatus parseStatus(String state) {
        if ("open".equalsIgnoreCase(state)) {
            return GithubIssue.IssueStatus.OPEN;
        }
        return GithubIssue.IssueStatus.CLOSED;
    }

    private String getTextOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTime).toLocalDateTime();
    }
}

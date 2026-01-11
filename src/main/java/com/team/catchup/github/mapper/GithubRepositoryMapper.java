package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class GithubRepositoryMapper {

    public GithubRepository toEntity(JsonNode apiResponse) {
        try {
            return GithubRepository.builder()
                    .repositoryId(apiResponse.get("id").asLong())
                    .owner(apiResponse.get("owner").get("login").asText())
                    .name(apiResponse.get("name").asText())
                    .description(getTextOrNull(apiResponse, "description"))
                    .primaryLanguage(getTextOrNull(apiResponse, "language"))
                    .isPrivate(apiResponse.get("private").asBoolean())
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .syncStatus(GithubRepository.SyncStatus.PENDING)
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map repository", e);
            throw new RuntimeException("Failed to map repository", e);
        }
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

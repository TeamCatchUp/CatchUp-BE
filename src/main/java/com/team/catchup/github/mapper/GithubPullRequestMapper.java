package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubPullRequest;
import com.team.catchup.github.entity.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class GithubPullRequestMapper {

    public GithubPullRequest toEntity(JsonNode apiResponse, GithubRepository repository) {
        try {
            GithubPullRequest.PullRequestStatus status = determineStatus(apiResponse);

            return GithubPullRequest.builder()
                    .pullRequestId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .number(apiResponse.get("number").asInt())
                    .title(apiResponse.get("title").asText())
                    .status(status)
                    .authorLogin(apiResponse.get("user").get("login").asText())
                    .baseBranch(apiResponse.get("base").get("ref").asText())
                    .headBranch(apiResponse.get("head").get("ref").asText())
                    .mergeCommitSha(getTextOrNull(apiResponse, "merge_commit_sha"))
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .closedAt(parseDateTime(getTextOrNull(apiResponse, "closed_at")))
                    .mergedAt(parseDateTime(getTextOrNull(apiResponse, "merged_at")))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map pull request", e);
            throw new RuntimeException("Failed to map pull request", e);
        }
    }

    private GithubPullRequest.PullRequestStatus determineStatus(JsonNode apiResponse) {
        String state = apiResponse.get("state").asText();
        JsonNode mergedAt = apiResponse.get("merged_at");

        if ("open".equalsIgnoreCase(state)) {
            return GithubPullRequest.PullRequestStatus.OPEN;
        } else if ("closed".equalsIgnoreCase(state)) {
            if (mergedAt != null && !mergedAt.isNull()) {
                return GithubPullRequest.PullRequestStatus.MERGED;
            }
            return GithubPullRequest.PullRequestStatus.CLOSED;
        }
        return GithubPullRequest.PullRequestStatus.CLOSED;
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

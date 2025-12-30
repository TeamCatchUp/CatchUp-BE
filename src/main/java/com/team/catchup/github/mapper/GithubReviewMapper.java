package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubPullRequest;
import com.team.catchup.github.entity.GithubRepository;
import com.team.catchup.github.entity.GithubReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class GithubReviewMapper {

    public GithubReview toEntity(JsonNode apiResponse, GithubRepository repository, GithubPullRequest pullRequest) {
        try {
            return GithubReview.builder()
                    .reviewId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .pullRequest(pullRequest)
                    .reviewerLogin(apiResponse.get("user").get("login").asText())
                    .reviewState(parseReviewState(apiResponse.get("state").asText()))
                    .submittedAt(parseDateTime(getTextOrNull(apiResponse, "submitted_at")))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map review", e);
            throw new RuntimeException("Failed to map review", e);
        }
    }

    private GithubReview.ReviewState parseReviewState(String state) {
        return switch (state.toUpperCase()) {
            case "APPROVED" -> GithubReview.ReviewState.APPROVED;
            case "CHANGES_REQUESTED" -> GithubReview.ReviewState.CHANGES_REQUESTED;
            case "COMMENTED" -> GithubReview.ReviewState.COMMENTED;
            case "DISMISSED" -> GithubReview.ReviewState.DISMISSED;
            case "PENDING" -> GithubReview.ReviewState.PENDING;
            default -> {
                log.warn("[GITHUB][MAPPER] Unknown review state: {}, defaulting to COMMENTED", state);
                yield GithubReview.ReviewState.COMMENTED;
            }
        };
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

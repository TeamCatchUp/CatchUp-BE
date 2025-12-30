package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubComment;
import com.team.catchup.github.entity.GithubIssue;
import com.team.catchup.github.entity.GithubPullRequest;
import com.team.catchup.github.entity.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class GithubCommentMapper {

    public GithubComment toIssueCommentEntity(JsonNode apiResponse, GithubRepository repository, GithubIssue issue) {
        try {
            return GithubComment.builder()
                    .commentId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .commentType(GithubComment.CommentType.ISSUE_COMMENT)
                    .issue(issue)
                    .authorLogin(apiResponse.get("user").get("login").asText())
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map issue comment", e);
            throw new RuntimeException("Failed to map issue comment", e);
        }
    }

    public GithubComment toReviewCommentEntity(JsonNode apiResponse, GithubRepository repository, GithubPullRequest pullRequest) {
        try {
            return GithubComment.builder()
                    .commentId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .commentType(GithubComment.CommentType.REVIEW_COMMENT)
                    .pullRequest(pullRequest)
                    .commitSha(getTextOrNull(apiResponse, "commit_id"))
                    .authorLogin(apiResponse.get("user").get("login").asText())
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map review comment", e);
            throw new RuntimeException("Failed to map review comment", e);
        }
    }

    public GithubComment toCommitCommentEntity(JsonNode apiResponse, GithubRepository repository, String commitSha) {
        try {
            return GithubComment.builder()
                    .commentId(apiResponse.get("id").asLong())
                    .repository(repository)
                    .commentType(GithubComment.CommentType.COMMIT_COMMENT)
                    .commitSha(commitSha)
                    .authorLogin(apiResponse.get("user").get("login").asText())
                    .createdAt(parseDateTime(apiResponse.get("created_at").asText()))
                    .updatedAt(parseDateTime(apiResponse.get("updated_at").asText()))
                    .htmlUrl(apiResponse.get("html_url").asText())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map commit comment", e);
            throw new RuntimeException("Failed to map commit comment", e);
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

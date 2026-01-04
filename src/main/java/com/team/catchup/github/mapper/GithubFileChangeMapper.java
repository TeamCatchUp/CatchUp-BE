package com.team.catchup.github.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.entity.GithubFileChange;
import com.team.catchup.github.entity.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GithubFileChangeMapper {

    public GithubFileChange toEntityFromCommit(JsonNode apiResponse, GithubRepository repository, String commitSha) {
        try {
            return GithubFileChange.builder()
                    .repository(repository)
                    .commitSha(commitSha)
                    .filePath(apiResponse.get("filename").asText())
                    .previousFilePath(getTextOrNull(apiResponse, "previous_filename"))
                    .changeType(parseChangeType(apiResponse.get("status").asText()))
                    .additions(apiResponse.get("additions").asInt())
                    .deletions(apiResponse.get("deletions").asInt())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map file change from commit", e);
            throw new RuntimeException("Failed to map file change from commit", e);
        }
    }

    public GithubFileChange toEntityFromPullRequest(JsonNode apiResponse, GithubRepository repository, com.team.catchup.github.entity.GithubPullRequest pullRequest) {
        try {
            return GithubFileChange.builder()
                    .repository(repository)
                    .pullRequest(pullRequest)
                    .filePath(apiResponse.get("filename").asText())
                    .previousFilePath(getTextOrNull(apiResponse, "previous_filename"))
                    .changeType(parseChangeType(apiResponse.get("status").asText()))
                    .additions(apiResponse.get("additions").asInt())
                    .deletions(apiResponse.get("deletions").asInt())
                    .build();
        } catch (Exception e) {
            log.error("[GITHUB][MAPPER] Failed to map file change from PR", e);
            throw new RuntimeException("Failed to map file change from PR", e);
        }
    }

    private GithubFileChange.FileChangeType parseChangeType(String status) {
        return switch (status.toLowerCase()) {
            case "added" -> GithubFileChange.FileChangeType.ADDED;
            case "modified" -> GithubFileChange.FileChangeType.MODIFIED;
            case "removed" -> GithubFileChange.FileChangeType.DELETED;
            case "renamed" -> GithubFileChange.FileChangeType.RENAMED;
            case "copied" -> GithubFileChange.FileChangeType.COPIED;
            default -> {
                log.warn("[GITHUB][MAPPER] Unknown file change type: {}, defaulting to MODIFIED", status);
                yield GithubFileChange.FileChangeType.MODIFIED;
            }
        };
    }

    private String getTextOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }
}

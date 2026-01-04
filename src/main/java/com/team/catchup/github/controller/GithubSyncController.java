package com.team.catchup.github.controller;

import com.team.catchup.auth.user.CustomOAuth2User;
import com.team.catchup.github.dto.GithubSyncStep;
import com.team.catchup.github.service.GithubSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/github/sync")
@RequiredArgsConstructor
@Slf4j
public class GithubSyncController {

    private final GithubSyncService githubSyncService;

    /**
     * Full Repository Sync
     * Repository 메타데이터, Commits, Pull Requests, Issues, Comments, Reviews, File Changes 모두 동기화
     *
     * @param authentication Authenticated user
     * @param owner Repository owner (username or organization)
     * @param repo Repository name
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, String>> fullSync(
            Authentication authentication,
            @RequestParam String owner,
            @RequestParam String repo
    ) {
        Long userId = extractUserId(authentication);
        log.info("[GITHUB][CONTROLLER] Full sync request received for {}/{} by userId: {}", owner, repo, userId);

        try {
            githubSyncService.fullSync(userId, owner, repo);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Full sync started for " + owner + "/" + repo
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to start full sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start sync: " + e.getMessage()
            ));
        }
    }

    /**
     * Retry Full Sync from a specific step
     * 특정 단계부터 Full Sync 재시도
     *
     * @param authentication Authenticated user
     * @param owner Repository owner (username or organization)
     * @param repo Repository name
     * @param startFrom Step to start from (REPOSITORY_INFO, COMMITS, PULL_REQUESTS, ISSUES, COMMENTS, REVIEWS, FILE_CHANGES)
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, String>> retryFromStep(
            Authentication authentication,
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam GithubSyncStep startFrom
    ) {
        Long userId = extractUserId(authentication);
        log.info("[GITHUB][CONTROLLER] Retry sync request received for {}/{} from step: {} by userId: {}",
                owner, repo, startFrom, userId);

        try {
            githubSyncService.fullSyncFrom(userId, owner, repo, startFrom);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", String.format("Retry sync started for %s/%s from step: %s",
                            owner, repo, startFrom)
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to start retry sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start retry sync: " + e.getMessage()
            ));
        }
    }

    /**
     * Repository 메타데이터만 동기화
     */
    @PostMapping("/repository")
    public ResponseEntity<Map<String, String>> syncRepository(
            @RequestParam String owner,
            @RequestParam String repo
    ) {
        log.info("[GITHUB][CONTROLLER] Repository sync request received for {}/{}", owner, repo);

        try {
            githubSyncService.syncRepositoryMetadata(owner, repo);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Repository metadata sync started for " + owner + "/" + repo
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to sync repository", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to sync repository: " + e.getMessage()
            ));
        }
    }

    /**
     * Commits 동기화
     *
     * @param since ISO 8601 timestamp (optional) - 이 시각 이후의 커밋만 가져옴
     */
    @PostMapping("/commits")
    public ResponseEntity<Map<String, String>> syncCommits(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam(required = false) String since
    ) {
        log.info("[GITHUB][CONTROLLER] Commits sync request received for {}/{}", owner, repo);

        try {
            githubSyncService.syncCommits(owner, repo, since);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Commits sync started for " + owner + "/" + repo
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to sync commits", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to sync commits: " + e.getMessage()
            ));
        }
    }

    /**
     * Pull Requests 동기화
     *
     * @param state open, closed, all (default: all)
     * @param since ISO 8601 timestamp (optional)
     */
    @PostMapping("/pull-requests")
    public ResponseEntity<Map<String, String>> syncPullRequests(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam(defaultValue = "all") String state,
            @RequestParam(required = false) String since
    ) {
        log.info("[GITHUB][CONTROLLER] Pull requests sync request received for {}/{}", owner, repo);

        try {
            githubSyncService.syncPullRequests(owner, repo, state, since);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Pull requests sync started for " + owner + "/" + repo
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to sync pull requests", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to sync pull requests: " + e.getMessage()
            ));
        }
    }

    /**
     * Issues 동기화
     *
     * @param state open, closed, all (default: all)
     * @param since ISO 8601 timestamp (optional)
     */
    @PostMapping("/issues")
    public ResponseEntity<Map<String, String>> syncIssues(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam(defaultValue = "all") String state,
            @RequestParam(required = false) String since
    ) {
        log.info("[GITHUB][CONTROLLER] Issues sync request received for {}/{}", owner, repo);

        try {
            githubSyncService.syncIssues(owner, repo, state, since);
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Issues sync started for " + owner + "/" + repo
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to sync issues", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to sync issues: " + e.getMessage()
            ));
        }
    }

    /**
     * Authentication 객체에서 userId 추출
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return oAuth2User.getMember().getId();
        }
        // JWT 인증의 경우 (UsernamePasswordAuthenticationToken)
        // Subject에 memberId가 String으로 저장되어 있음
        return Long.parseLong(authentication.getName());
    }
}

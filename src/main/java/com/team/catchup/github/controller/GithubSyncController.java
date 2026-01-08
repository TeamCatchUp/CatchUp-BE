package com.team.catchup.github.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.github.dto.GithubSyncStep;
import com.team.catchup.github.dto.request.GithubFullSyncRequest;
import com.team.catchup.github.dto.request.GithubRetryRequest;
import com.team.catchup.github.service.GithubSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, String>> fullSync(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GithubFullSyncRequest request
    ) {

        log.info("[GITHUB][CONTROLLER] Full sync request received for {}/{}@{}",
                request.owner(),
                request.repository(),
                request.branch()
        );

        try {
            githubSyncService.fullSync(userDetails.getMemberId(),
                    request.owner(),
                    request.repository());
            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Full sync started for " + request.owner() + "/" + request.repository() + "@" + request.branch()
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
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, String>> retryFromStep(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GithubRetryRequest request
    ) {
        log.info("[GITHUB][CONTROLLER] Retry sync request received for {}/{}@{} from step: {}",
                request.owner(), request.repository(), request.branch(), request.startFrom());

        try {
            githubSyncService.fullSyncFrom(
                    userDetails.getMemberId(),
                    request.owner(),
                    request.repository(),
                    request.startFrom()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", String.format("Retry sync started for %s/%s from step: %s",
                            request.owner(), request.repository(), request.startFrom())
            ));
        } catch (Exception e) {
            log.error("[GITHUB][CONTROLLER] Failed to start retry sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start retry sync: " + e.getMessage()
            ));
        }
    }
}

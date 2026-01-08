package com.team.catchup.jira.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.jira.dto.request.JiraSyncRequest;
import com.team.catchup.jira.service.JiraSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jira/sync")
@RequiredArgsConstructor
public class JiraSyncController {

    private final JiraSyncService jiraSyncService;

    /**
     * 전체 동기화 (처음부터)
     * POST /api/jira/sync/full
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, String>> fullSync(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[API] Full Sync 요청");

        jiraSyncService.fullSync(userDetails.getMemberId());

        return ResponseEntity.ok(Map.of(
                "status", "started",
                "message", "Jira Full Sync Started"
        ));
    }

    /**
     * 특정 단계부터 동기화 재시도
     * POST /api/jira/sync/retry
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, String>> retrySync(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody JiraSyncRequest request) {
        log.info("[API] Retry Sync 요청 - StartFrom: {}, ProjectKeys: {}", request.startFrom(), request.projectKeys());

        jiraSyncService.fullSyncFrom(userDetails.getMemberId(), request.startFrom(), request.projectKeys());
        return ResponseEntity.ok(Map.of(
                "status", "started",
                "message", String.format("Jira Retry Sync Started from step: %s", request.startFrom())
        ));
    }

    /**
     * 실패한 프로젝트만 재시도
     * POST /api/jira/sync/retry/projects
     * Body: ["PROJ-A", "PROJ-B"]
     */
    @PostMapping("/retry/projects")
    public ResponseEntity<Map<String, String>> retryFailedProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody JiraSyncRequest request
    ) {
        log.info("[JIRA][CONTROLLER] Retry Failed Projects request - Projects: {}",
                request.projectKeys());

        if (request.projectKeys() == null || request.projectKeys().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Project keys are required"
            ));
        }

        jiraSyncService.retryFailedProjects(request.projectKeys());

        return ResponseEntity.ok(Map.of(
                "status", "started",
                "message", "Jira Retry Failed Projects Process Started"
        ));
    }
}
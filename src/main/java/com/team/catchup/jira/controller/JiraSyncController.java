package com.team.catchup.jira.controller;

import com.team.catchup.jira.dto.SyncStep;
import com.team.catchup.jira.dto.response.FullSyncResult;
import com.team.catchup.jira.service.JiraSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<FullSyncResult> fullSync() {
        log.info("[API] Full Sync 요청");
        FullSyncResult result = jiraSyncService.fullSync();
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 단계부터 동기화 재시도
     * POST /api/jira/sync/retry?startFrom=USERS&projectKeys=PROJ-A,PROJ-B
     */
    @PostMapping("/retry")
    public ResponseEntity<FullSyncResult> retrySync(
            @RequestParam(name = "startFrom") SyncStep startFrom,
            @RequestParam(name = "projectKeys", required = false) List<String> projectKeys
    ) {
        log.info("[API] Retry Sync 요청 - StartFrom: {}, ProjectKeys: {}", startFrom, projectKeys);
        FullSyncResult result = jiraSyncService.fullSyncFrom(startFrom, projectKeys);
        return ResponseEntity.ok(result);
    }

    /**
     * 실패한 프로젝트만 재시도
     * POST /api/jira/sync/retry/projects
     * Body: ["PROJ-A", "PROJ-B"]
     */
    @PostMapping("/retry/projects")
    public ResponseEntity<FullSyncResult> retryFailedProjects(
            @RequestBody List<String> failedProjectKeys
    ) {
        log.info("[API] Retry Failed Projects 요청 - Projects: {}", failedProjectKeys);
        FullSyncResult result = jiraSyncService.retryFailedProjects(failedProjectKeys);
        return ResponseEntity.ok(result);
    }
}
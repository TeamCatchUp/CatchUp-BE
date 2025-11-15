package com.team.catchup.jira.controller;

import com.team.catchup.jira.service.JiraSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jira/sync")
@RequiredArgsConstructor
public class JiraSyncController {

    private final JiraSyncService jiraSyncService;

    /**
     * Full Sync
     * GET /api/jira/sync/full?projectKey=BJDD&maxResults=100
     */
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> fullSync(
            @RequestParam String projectKey,
            @RequestParam(defaultValue = "100") Integer maxResults
    ) {
        log.info("[API] Full Sync 요청 - Project: {}, MaxResults: {}", projectKey, maxResults);

        try {
            // 전체 이슈 개수 조회
            Integer totalCount = jiraSyncService.getTotalIssueCount(projectKey);
            log.info("[API] 총 이슈 개수: {}", totalCount);

            // 동기화 실행
            jiraSyncService.fullSync(projectKey, maxResults);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Full sync completed");
            response.put("projectKey", projectKey);
            response.put("totalIssues", totalCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[API] Full Sync 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Full sync failed: " + e.getMessage());
            errorResponse.put("projectKey", projectKey);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 프로젝트의 전체 이슈 개수만 조회
     * GET /api/jira/sync/count?projectKey=BJDD
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getIssueCount(
            @RequestParam String projectKey
    ) {
        log.info("[API] Issue Count 요청 - Project: {}", projectKey);

        try {
            Integer totalCount = jiraSyncService.getTotalIssueCount(projectKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("projectKey", projectKey);
            response.put("totalIssues", totalCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[API] Issue Count 조회 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get issue count: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/issue-types")
    public ResponseEntity<Map<String, Object>> syncIssueTypes() {
        log.info("[API] IssueType Sync 요청");

        try {
            // 동기화 실행
            jiraSyncService.syncAllIssueTypes();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "IssueType sync completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[API] IssueType Sync 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "IssueType sync failed: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
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
    @PostMapping("/full")
    public ResponseEntity<Map<String, Object>> fullSync(
            @RequestParam(name = "projectKey") String projectKey,
            @RequestParam(name = "maxResults", defaultValue = "100") Integer maxResults
    ) {
        log.info("[API] Full Sync 요청 - Project: {}, MaxResults: {}", projectKey, maxResults);

        try {
            // 동기화 실행
            int totalSaved = jiraSyncService.fullSync(projectKey, maxResults);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Full sync completed");
            response.put("projectKey", projectKey);
            response.put("totalSaved", totalSaved);

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

    @PostMapping("/issueType")
    public ResponseEntity<Map<String, Object>> syncIssueType(
            @RequestParam(name = "projectKey") String projectKey
    ) {
        try{
            int totalSaved = jiraSyncService.syncAllIssueTypes();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Issue Type sync completed");
            response.put("projectKey", projectKey);
            response.put("totalSaved", totalSaved);

            return ResponseEntity.ok(response);
    } catch (Exception e) {
            log.error("[API] Issue Type Sync 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Issue Type sync failed: " + e.getMessage());
            errorResponse.put("projectKey", projectKey);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> syncUsers() {
        log.info("[API] Jira User Sync 요청");

        try {
            jiraSyncService.syncAllUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Jira User sync completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[API] Jira User Sync 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Jira User sync failed: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> syncProjects() {
        log.info("[API] Project Sync 요청");

        try {
            // 동기화 실행
            jiraSyncService.syncAllProjects();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Project sync completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[API] Project Sync 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Project sync failed: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
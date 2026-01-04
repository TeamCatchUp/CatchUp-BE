package com.team.catchup.jira.controller;

import com.team.catchup.auth.user.CustomOAuth2User;
import com.team.catchup.jira.dto.request.JiraSyncRequest;
import com.team.catchup.jira.service.JiraSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> fullSync(Authentication authentication) {
        Long userId = extractUserId(authentication);
        log.info("[API] Full Sync 요청 - userId: {}", userId);
        jiraSyncService.fullSync(userId);
        return ResponseEntity.ok("Jira Full Sync Process Started at Background");
    }

    /**
     * 특정 단계부터 동기화 재시도
     * POST /api/jira/sync/retry
     * Body: { "startFrom": "USERS", "projectKeys": ["PROJ-A", "PROJ-B"] }
     */
    @PostMapping("/retry")
    public ResponseEntity<String> retrySync(
            Authentication authentication,
            @RequestBody JiraSyncRequest request
    ) {
        Long userId = extractUserId(authentication);
        log.info("[API] Retry Sync 요청 - userId: {}, StartFrom: {}, ProjectKeys: {}",
                userId, request.startFrom(), request.projectKeys());
        jiraSyncService.fullSyncFrom(userId, request.startFrom(), request.projectKeys());
        return ResponseEntity.ok("Jira Retry Sync Process Started at Background");
    }

    /**
     * 실패한 프로젝트만 재시도
     * POST /api/jira/sync/retry/projects
     * Body: { "projectKeys": ["PROJ-A", "PROJ-B"] }
     */
    @PostMapping("/retry/projects")
    public ResponseEntity<String> retryFailedProjects(@RequestBody JiraSyncRequest request) {
        log.info("[API] Retry Failed Projects 요청 - Projects: {}", request.projectKeys());
        jiraSyncService.retryFailedProjects(request.projectKeys());
        return ResponseEntity.ok("Jira Retry Failed Projects Process Started at Background");
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
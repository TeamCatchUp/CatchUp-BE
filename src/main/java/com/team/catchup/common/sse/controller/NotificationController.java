package com.team.catchup.common.sse.controller;

import com.team.catchup.auth.user.CustomOAuth2User;
import com.team.catchup.common.sse.service.SyncNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final SyncNotificationService notificationService;

    @GetMapping(value = "/subscribe/sync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        Long userId = extractUserId(authentication);
        log.info("[API][SSE Subscribe Request for Syncing Tool | userId : {}", userId);
        return notificationService.subscribe(userId);
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

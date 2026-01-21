package com.team.catchup.auth.controller;

import com.team.catchup.auth.service.AuthService;
import com.team.catchup.auth.service.CookieService;
import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.auth.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh Token이 없습니다."));
        }

        String newAccessToken = authService.refreshAccessToken(refreshToken)
                .accessToken();

        response.addHeader("Set-Cookie",
                cookieService.createAccessTokenCookie(newAccessToken).toString());

        return ResponseEntity.ok(Map.of(
                "message", "Access Token이 갱신되었습니다."
        ));
    }

    // @CookieValue으로 개선가능 -> Swagger 테스트 목적상 Header 방식 가능하도록 유지
    @PostMapping("/api/auth/logout")
    public ResponseEntity<Map<String, String>> logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = TokenExtractor.extractAccessToken(request);

        if (accessToken != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long memberId = userDetails.getMemberId();
            authService.logout(memberId, accessToken);
        }

        response.addHeader("Set-Cookie",
                cookieService.createAccessTokenDeleteCookie().toString());
        response.addHeader("Set-Cookie",
                cookieService.createRefreshTokenDeleteCookie().toString());

        return ResponseEntity.ok(Map.of(
                "message", "로그아웃되었습니다."
        ));
    }
}
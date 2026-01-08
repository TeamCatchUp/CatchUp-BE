package com.team.catchup.auth.controller;

import com.team.catchup.auth.JwtTokenResponse;
import com.team.catchup.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 구글 로그인 성공 후 리디렉트 되어 JWT 토큰을 발급한다.
     */
    @GetMapping("/oauth/callback")
    public JwtTokenResponse issueJwtToken(@RequestParam String token) {
        return JwtTokenResponse.from(token);
    }


    /**
     *  Refresh Token을 사용하여 새로운 Access Token을 발급한다.
     */
    @PostMapping("/api/auth/refresh")
    public JwtTokenResponse refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/api/auth/logout")
    public void logout(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = authHeader.substring(7);
        Long memberId = Long.valueOf(authentication.getName());
        authService.logout(memberId, accessToken);
    }
}

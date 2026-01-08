package com.team.catchup.auth.handler;

import com.team.catchup.auth.jwt.JwtTokenProvider;
import com.team.catchup.auth.service.RefreshTokenService;
import com.team.catchup.auth.user.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getMember().getEmail();
        String role = oAuth2User.getMember().getRole().name();
        Long memberId = oAuth2User.getMember().getId();

        log.info("Google 로그인 성공");

        // JSESSIONID 무효화
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        String accessToken = tokenProvider.createAccessToken(email, role, memberId);
        String refreshToken = tokenProvider.createRefreshToken(memberId);

        refreshTokenService.saveRefreshToken(memberId, refreshToken);

        log.info("=".repeat(100));
        log.info("JWT Tokens for: {}", email);
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);
        log.info("=".repeat(100));

        response.addHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect(redirectUri + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
    }
}

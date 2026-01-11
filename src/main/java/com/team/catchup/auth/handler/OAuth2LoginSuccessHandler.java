package com.team.catchup.auth.handler;

import com.team.catchup.auth.jwt.JwtTokenProvider;
import com.team.catchup.auth.service.CookieService;
import com.team.catchup.auth.service.RefreshTokenService;
import com.team.catchup.auth.user.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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
    private final CookieService cookieService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getMember().getEmail();
        String role = oAuth2User.getMember().getRole().name();
        Long memberId = oAuth2User.getMember().getId();

        log.info("Google 로그인 성공 - email: {}", email);

        // JSESSIONID 무효화
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        Cookie jsessionCookie = new Cookie("JSESSIONID", null);
        jsessionCookie.setPath("/");
        jsessionCookie.setMaxAge(0);
        jsessionCookie.setHttpOnly(true);
        response.addCookie(jsessionCookie);

        String accessToken = tokenProvider.createAccessToken(email, role, memberId);
        String refreshToken = tokenProvider.createRefreshToken(memberId);

        refreshTokenService.saveRefreshToken(memberId, refreshToken);

        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(refreshToken);

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("JWT 토큰 쿠키 발급 완료 - memberId: {}", memberId);

        response.sendRedirect(redirectUri);
    }
}
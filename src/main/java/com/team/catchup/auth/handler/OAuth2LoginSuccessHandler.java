package com.team.catchup.auth.handler;

import com.team.catchup.auth.jwt.JwtTokenProvider;
import com.team.catchup.auth.user.CustomOAuth2User;
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

        String accessToken = tokenProvider.createAccessToken(email, role, memberId);

        response.addHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect(redirectUri); // 프론트엔드 홈화면으로 리다이렉트
    }
}

package com.team.catchup.auth.handler;

import com.team.catchup.auth.jwt.JwtTokenProvider;
import com.team.catchup.auth.user.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getMember().getEmail();
        String role = oAuth2User.getMember().getRole().name();

        log.info("Google 로그인 성공");

        String accessToken = tokenProvider.createAccessToken(email, role);
        response.addHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect("http://localhost:8080/oauth/callback?token=" + accessToken); // TODO: 개선
    }
}

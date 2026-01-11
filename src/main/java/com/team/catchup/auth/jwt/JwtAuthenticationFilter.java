package com.team.catchup.auth.jwt;

import com.team.catchup.auth.service.TokenBlacklistService;
import com.team.catchup.auth.util.TokenExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = TokenExtractor.extractAccessToken(request);

        if (StringUtils.hasText(token)) {
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.debug("Blacklisted token");
                filterChain.doFilter(request, response);
                return;
            }

            if (tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.debug("유효하지 않은 토큰");
            }
        } else {
            log.debug("토큰 없음");
        }

        filterChain.doFilter(request, response);
    }
}
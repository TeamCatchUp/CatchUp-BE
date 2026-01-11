package com.team.catchup.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int ACCESS_TOKEN_MAX_AGE = 3600;
    private static final int REFRESH_TOKEN_MAX_AGE = 604800;

    @Value("${jwt.cookie.secure:}")
    private boolean secure;

    @Value("${jwt.cookie.domain:}")
    private String domain;

    @Value("${jwt.cookie.same-site:}")
    private String sameSite;

    public ResponseCookie createAccessTokenCookie(String token) {
        return createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, token, ACCESS_TOKEN_MAX_AGE);
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createResponseCookie(REFRESH_TOKEN_COOKIE_NAME, token, REFRESH_TOKEN_MAX_AGE);
    }

    public ResponseCookie createAccessTokenDeleteCookie() {
        return createDeleteResponseCookie(ACCESS_TOKEN_COOKIE_NAME);
    }

    public ResponseCookie createRefreshTokenDeleteCookie() {
        return createDeleteResponseCookie(REFRESH_TOKEN_COOKIE_NAME);
    }

    private ResponseCookie createResponseCookie(String name, String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite(sameSite);  // ✅ SameSite 설정

        if (domain != null && !domain.isEmpty()) {
            builder.domain(domain);
        }

        return builder.build();
    }

    private ResponseCookie createDeleteResponseCookie(String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite);

        if (domain != null && !domain.isEmpty()) {
            builder.domain(domain);
        }

        return builder.build();
    }
}
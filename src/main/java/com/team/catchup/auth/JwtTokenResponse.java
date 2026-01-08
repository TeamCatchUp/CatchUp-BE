package com.team.catchup.auth;

public record JwtTokenResponse(
        String accessToken,
        String refreshToken
) {
    public static JwtTokenResponse from(String accessToken){
        return new JwtTokenResponse(accessToken, null);
    }

    public static JwtTokenResponse of(String accessToken, String refreshToken){
        return new JwtTokenResponse(accessToken, refreshToken);
    }
}

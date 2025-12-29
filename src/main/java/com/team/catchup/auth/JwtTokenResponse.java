package com.team.catchup.auth;

public record JwtTokenResponse(
        String accessToken
) {
    public static JwtTokenResponse from(String accessToken){
        return new JwtTokenResponse(accessToken);
    }
}

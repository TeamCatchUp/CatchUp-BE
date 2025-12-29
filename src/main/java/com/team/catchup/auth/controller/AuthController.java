package com.team.catchup.auth.controller;

import com.team.catchup.auth.JwtTokenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    /**
     * 구글 로그인 성공 후 리디렉트 되어 JWT 토큰을 발급한다.
     */
    @GetMapping("/oauth/callback")
    public JwtTokenResponse issueJwtToken(@RequestParam String token) {
        return JwtTokenResponse.from(token);
    }
}

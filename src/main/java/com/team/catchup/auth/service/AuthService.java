package com.team.catchup.auth.service;

import com.team.catchup.auth.JwtTokenResponse;
import com.team.catchup.auth.jwt.JwtTokenProvider;
import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public JwtTokenResponse refreshAccessToken(String refreshToken) {

        if(!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if(!tokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token Type is not REFRESH_TOKEN");
        }

        Long memberId = tokenProvider.getMemberIdFromToken(refreshToken);

        if(!refreshTokenService.validateRefreshToken(memberId, refreshToken)) {
            throw new RuntimeException("Token Doesn't Match Saved Token");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member Not Found"));

        String newAccessToken = tokenProvider.createAccessToken(
                member.getEmail(),
                member.getRole().name(),
                member.getId()
        );

        return JwtTokenResponse.from(newAccessToken);
    }

    @Transactional
    public void logout(Long memberId, String accessToken) {
        tokenBlacklistService.addToBlacklist(accessToken);

        refreshTokenService.deleteRefreshToken(memberId);
    }
}

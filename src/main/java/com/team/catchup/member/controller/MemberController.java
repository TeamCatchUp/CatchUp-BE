package com.team.catchup.member.controller;

import com.team.catchup.member.dto.MemberInfoResponse;
import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/api/me")
    public MemberInfoResponse getMyInfo(Authentication authentication) {
        Long memberId = Long.valueOf(authentication.getName());

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("member not found"));

        return new MemberInfoResponse(member.getName(), member.getEmail());
    }
}

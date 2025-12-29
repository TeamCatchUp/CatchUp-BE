package com.team.catchup.auth.service;

import com.team.catchup.auth.user.CustomOAuth2User;
import com.team.catchup.member.entity.Member;
import com.team.catchup.member.enums.MemberRoleType;
import com.team.catchup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object>  attributes = oAuth2User.getAttributes();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        Member member = saveOrUpdate(email, name, providerId, registrationId);

        return new CustomOAuth2User(member, attributes);
    }

    private Member saveOrUpdate(String email, String name, String providerId, String provider){
        Member member = memberRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElse(Member.builder()
                        .email(email)
                        .name(name)
                        .role(MemberRoleType.ROLE_MEMBER)
                        .provider(provider)
                        .providerId(providerId)
                        .build());

        return memberRepository.save(member);
    }

}

package com.team.catchup.member.entity;

import com.team.catchup.member.enums.MemberRoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 구글 이메일을 로그인 ID로 사용

    private String name; // 구글 계정 상 이름

    private String realName; // 실명 (회사 정책상 구글 계정의 이름과 같다면 추후 삭제)

    @Enumerated(EnumType.STRING)
    private MemberRoleType role;

    private String provider; // "google"
    private String providerId; // 구글의 'sub' 값 (고유 식별자)

    public Member update(String name) {
        this.name = name;
        return this;
    }
}
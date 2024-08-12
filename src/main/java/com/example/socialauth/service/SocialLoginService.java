package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import com.example.socialauth.oauth2.OAuth2Attributes;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final MemberRepository memberRepository;

    public Member findMemberByLoginIdAndLoginType(String loginId, Member.LoginType type) {
        return memberRepository.findByLoginIdAndLoginType(loginId, type);
    }

    public void handleSocialLogin(HttpSession session, String loginId, String name, String email, String loginType) {
        if (loginType == null) {
            throw new IllegalArgumentException("Login type cannot be null");
        }

        Member.LoginType type = Member.LoginType.valueOf(loginType.toUpperCase());

        try {
            Member member = findMemberByLoginIdAndLoginType(loginId, type);
            if (member == null) {
                // 회원이 존재하지 않는 경우 회원가입 처리
                member = registerNewMember(name, loginId, email, type);
            }
            session.setAttribute("member", member);
        } catch (Exception e) {
            // 예외가 발생한 경우 사용자의 속성을 세션에 저장
            session.setAttribute("userAttributes", Map.of("name", name, "email", email, "loginType", loginType, "loginId", loginId));
        }
    }

    public Member processUserLogin(OAuth2Attributes attributes) {
        // OAuth2Attributes에서 필요한 정보를 추출하여 로그인 ID 및 로그인 타입 설정
        String loginId = attributes.getLoginId();
        Member.LoginType loginType = attributes.getLoginType();

        // 사용자가 이미 존재하는지 확인
        Member member = findMemberByLoginIdAndLoginType(loginId, loginType);

        if (member == null) {
            // 회원이 존재하지 않는 경우 회원가입 처리
            member = registerNewMember(attributes.getName(), loginId, attributes.getEmail(), loginType);
        }

        return member;
    }

    private Member registerNewMember(String name, String loginId, String email, Member.LoginType loginType) {
        Member member = new Member();
        member.setName(name);
        member.setLoginId(loginId);
        member.setEmail(email);
        member.setLoginType(loginType);
        member.setRole(Member.Role.USER); // 기본 역할을 USER로 설정
        member.setActive(true);

        return save(member);
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}

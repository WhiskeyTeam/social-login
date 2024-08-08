package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SocialLoginService {

    private final MemberRepository memberRepository;

    @Autowired
    public SocialLoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Optional<Member> findMemberByGoogleSub(String googleSub) {
        return memberRepository.findByGoogleSub(googleSub);
    }

    public Optional<Member> findMemberByNaverId(String naverId) {
        return memberRepository.findByNaverId(naverId);
    }

    public void handleSocialLogin(HttpSession session, String loginId, String name, String email, String loginType) {
        Optional<Member> member = Optional.empty();

        if ("Google".equalsIgnoreCase(loginType)) {
            member = findMemberByGoogleSub(loginId);
        } else if ("Naver".equalsIgnoreCase(loginType)) {
            member = findMemberByNaverId(loginId);
        }

        if (member.isPresent()) {
            // 이미 회원이 존재하면 로그인 처리 (세션에 회원 정보 저장 등)
            session.setAttribute("member", member.get());
        } else {
            // 회원 정보가 없으면 회원가입 페이지로 리디렉트
            // 세션에 소셜 로그인 사용자 정보를 저장
            Map<String, String> userAttributes = new HashMap<>();
            if (name != null) userAttributes.put("name", name);
            if (email != null) userAttributes.put("email", email);
            if (loginType != null) userAttributes.put("loginType", loginType);

            session.setAttribute("userAttributes", userAttributes);
        }
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}

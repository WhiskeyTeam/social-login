package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class SocialLoginService {

    private final MemberRepository memberRepository;

    @Autowired
    public SocialLoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Optional<Member> findMemberByLoginIdAndLoginType(String loginId, Member.LoginType type) {
        return memberRepository.findByLoginIdAndLoginType(loginId, type);
    }

    public void handleSocialLogin(HttpSession session, String loginId, String name, String email, String loginType) {
        if (loginType == null) {
            throw new IllegalArgumentException("Login type cannot be null");
        }

        Member.LoginType type = Member.LoginType.valueOf(loginType.toUpperCase());

        Optional<Member> member = findMemberByLoginIdAndLoginType(loginId, type);

        if (member.isPresent()) {
            session.setAttribute("member", member.get());
        } else {
            session.setAttribute("userAttributes", Map.of("name", name, "email", email, "loginType", loginType, "loginId", loginId));
        }
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
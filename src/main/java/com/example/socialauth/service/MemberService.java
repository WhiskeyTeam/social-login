package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member registerMember(String name, String nickname, String loginId, String email, String password, Member.LoginType loginType) {
        Member member = new Member();
        member.setName(name);
        member.setNickname(nickname);
        member.setLoginId(loginId);
        member.setEmail(email);
        member.setPassword(password != null ? passwordEncoder.encode(password) : null);
        member.setLoginType(loginType);

        return memberRepository.save(member);
    }

    public Member registerSocialMember(String name, String nickname, String loginId, String email, Member.LoginType loginType) {
        return registerMember(name, nickname, loginId, email, null, loginType);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }
}

package com.example.socialauth.service;

import com.example.socialauth.entity.LoginType;
import com.example.socialauth.entity.Member;
import com.example.socialauth.entity.Role;
import com.example.socialauth.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberManagementService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }

    public Member registerMember(String name, String nickname, String loginId, String email, String password, LoginType loginType) {
        if (findByLoginId(loginId) != null) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        Member member = new Member();
        member.setName(name);
        member.setNickname(nickname);
        member.setLoginId(loginId);
        member.setEmail(email);
        member.setPassword(password != null ? passwordEncoder.encode(password) : null);
        member.setLoginType(loginType != null ? loginType : LoginType.BASIC);
        member.setRole(Role.USER);
        member.setActive(true);

        return memberRepository.save(member);
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}

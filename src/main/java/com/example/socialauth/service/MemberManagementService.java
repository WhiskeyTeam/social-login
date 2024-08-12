package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

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
        try {
            return memberRepository.findByLoginId(loginId);
        } catch (Exception e) {
            throw new EntityNotFoundException("회원이 존재하지 않습니다.");
        }
    }

    public Member findByLoginIdAndLoginType(String loginId, Member.LoginType loginType) {
        try {
            return memberRepository.findByLoginIdAndLoginType(loginId, loginType);
        } catch (Exception e) {
            throw new EntityNotFoundException("해당 로그인 타입의 회원이 존재하지 않습니다.");
        }
    }

    public Member registerMember(String name, String nickname, String loginId, String email, String password, Member.LoginType loginType) {
        try {
            findByLoginId(loginId);
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        } catch (EntityNotFoundException e) {
            Member member = new Member();
            member.setName(name);
            member.setNickname(nickname);
            member.setLoginId(loginId);
            member.setEmail(email);
            member.setPassword(password != null ? passwordEncoder.encode(password) : null);
            member.setLoginType(loginType != null ? loginType : Member.LoginType.BASIC);
            member.setRole(Member.Role.USER);
            member.setActive(true);

            return memberRepository.save(member);
        }
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}

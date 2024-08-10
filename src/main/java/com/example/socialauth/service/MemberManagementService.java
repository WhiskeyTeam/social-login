package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberManagementService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 로그인 ID를 이용하여 회원 정보를 조회합니다.
     *
     * @param loginId 로그인 ID
     * @return 회원 정보 Optional 객체
     */
    public Optional<Member> findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }

    /**
     * 새로운 회원을 등록합니다.
     *
     * @param name     회원 이름
     * @param nickname 회원 닉네임
     * @param loginId  로그인 ID
     * @param email    이메일
     * @param password 비밀번호 (없을 경우 null)
     * @return 등록된 회원 정보
     */
    public Member registerMember(String name, String nickname, String loginId, String email, String password) {
        Member member = new Member();
        member.setName(name);
        member.setNickname(nickname);
        member.setLoginId(loginId);
        member.setEmail(email);
        member.setPassword(password != null ? passwordEncoder.encode(password) : null);
        member.setLoginType(Member.LoginType.BASIC);
        member.setRole(Member.Role.USER);
        member.setActive(true);

        try {
            return memberRepository.save(member);
        } catch (Exception e) {
            System.err.println("회원 저장 중 오류 발생: " + e.getMessage());
            return null;
        }
    }

    /**
     * 회원 정보를 저장합니다.
     *
     * @param member 회원 객체
     * @return 저장된 회원 객체
     */
    public Member save(Member member) {
        return memberRepository.save(member);
    }
}

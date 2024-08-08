package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 소셜 로그인 관련 서비스를 처리하는 클래스
 */
@Service
public class SocialLoginService {

    private final MemberRepository memberRepository;

    @Autowired
    public SocialLoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 구글 Sub ID를 이용하여 회원 정보를 조회
     *
     * @param googleSub 구글 Sub ID
     * @return 회원 정보 Optional 객체
     */
    public Optional<Member> findMemberByGoogleSub(String googleSub) {
        return memberRepository.findByGoogleSub(googleSub);
    }

    /**
     * 네이버 ID를 이용하여 회원 정보를 조회함
     *
     * @param naverId 네이버 ID
     * @return 회원 정보 Optional 객체
     */
    public Optional<Member> findMemberByNaverId(String naverId) {
        return memberRepository.findByNaverId(naverId);
    }

    /**
     * 소셜 로그인 처리 로직을 수행
     * 세션에 사용자 정보를 저장하거나 회원 가입 페이지로 리디렉션
     *
     * @param session  세션 객체
     * @param loginId  로그인 ID
     * @param name     사용자 이름
     * @param email    사용자 이메일
     * @param loginType 로그인 유형
     */
    public void handleSocialLogin(HttpSession session, String loginId, String name, String email, String loginType) {
        Optional<Member> member = Optional.empty();

        // 로그인 유형에 따라 구글 또는 네이버 ID로 회원 조회
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

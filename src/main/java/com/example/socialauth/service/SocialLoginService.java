package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final MemberRepository memberRepository;

    public Member handleSocialLogin(String loginId, String name, String email, Member.LoginType loginType) {
        Member existingMember = memberRepository.findByLoginId(loginId);

        if (existingMember == null) {
            // 회원이 존재하지 않는 경우 새로운 회원 등록
            return registerSocialMember(name, "", loginId, email, loginType);
        }

        return existingMember;
    }

    private Member registerSocialMember(String name, String nickname, String loginId, String email, Member.LoginType loginType) {
        Member member = new Member();
        member.setName(name);
        member.setNickname(nickname);
        member.setLoginId(loginId);
        member.setEmail(email);
        member.setLoginType(loginType);

        return memberRepository.save(member);
    }
}

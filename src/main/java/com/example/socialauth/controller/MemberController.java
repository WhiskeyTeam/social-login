package com.example.socialauth.controller;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    @Autowired
    private SocialLoginService socialLoginService;

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        model.addAttribute("userAttributes", session.getAttribute("userAttributes"));
        model.addAttribute("isSocialLogin", session.getAttribute("isSocialLogin"));
        return "register";
    }

    @PostMapping("/register_social")
    public String registerSocial(Member member, HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");
        String name = (String) session.getAttribute("name");
        String email = (String) session.getAttribute("email");
        String loginType = (String) session.getAttribute("loginType");

        // 소셜 로그인 회원가입 처리 로직
        member.setLoginId(loginId);
        member.setName(name);
        member.setEmail(email);
        member.setLoginType(Member.LoginType.valueOf(loginType));

        // 회원 정보를 저장하고 세션에서 제거
        socialLoginService.save(member);
        session.removeAttribute("loginId");
        session.removeAttribute("name");
        session.removeAttribute("email");
        session.removeAttribute("loginType");

        return "redirect:/success";
    }

    @PostMapping("/register")
    public String register(Member member) {
        // 일반 회원가입 처리 로직 추가
        socialLoginService.save(member);
        return "redirect:/success";
    }
}

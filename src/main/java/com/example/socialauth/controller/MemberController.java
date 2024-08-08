package com.example.socialauth.controller;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberManagementService;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@Controller
public class MemberController {

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private MemberManagementService memberManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        model.addAttribute("userAttributes", session.getAttribute("userAttributes"));
        model.addAttribute("isSocialLogin", session.getAttribute("isSocialLogin"));
        return "register";
    }

    @PostMapping("/register_social")
    public String registerSocial(Member member, HttpSession session) {
        Map<String, Object> userAttributes = (Map<String, Object>) session.getAttribute("userAttributes");

        String loginId = (String) userAttributes.get("loginId");
        String name = (String) userAttributes.get("name");
        String email = (String) userAttributes.get("email");
        String loginType = (String) userAttributes.get("loginType");

        // 소셜 로그인 회원가입 처리 로직
        member.setLoginId(loginId);
        member.setName(name);
        member.setEmail(email);
        member.setLoginType(Member.LoginType.valueOf(loginType));
        member.setRole(Member.Role.USER);  // 기본 역할 설정
        member.setActive(true);  // 기본 활성화 설정

        // 회원 정보를 저장하고 세션에서 제거
        socialLoginService.save(member);
        session.removeAttribute("userAttributes");
        session.removeAttribute("isSocialLogin");

        return "redirect:/success";
    }

    @PostMapping("/register")
    public String register(Member member) {
        // 일반 회원가입 처리 로직 추가
        member.setRole(Member.Role.USER);  // 기본 역할 설정
        member.setActive(true);  // 기본 활성화 설정
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberManagementService.save(member);
        return "redirect:/success";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId, @RequestParam String password, HttpSession session, Model model) {
        Optional<Member> optionalMember = memberManagementService.findByLoginId(loginId);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (passwordEncoder.matches(password, member.getPassword())) {
                // 로그인 성공 처리
                session.setAttribute("member", member);
                return "redirect:/success";
            } else {
                model.addAttribute("error", "Invalid password");
            }
        } else {
            model.addAttribute("error", "Member not found");
        }
        return "login";
    }
}

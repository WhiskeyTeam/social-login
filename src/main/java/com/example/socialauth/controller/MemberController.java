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

import java.util.Optional;

@Controller
public class MemberController {

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private MemberManagementService memberManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "index"; // index.html을 반환
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html을 반환
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<Member> optionalMember = memberManagementService.findByLoginId(username);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (passwordEncoder.matches(password, member.getPassword())) {
                // 로그인 성공 처리
                session.setAttribute("member", member);
                return "redirect:/success"; // 로그인 후 성공 페이지로 리디렉션
            } else {
                model.addAttribute("error", "Invalid password");
            }
        } else {
            model.addAttribute("error", "Member not found");
        }
        return "login";
    }

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

        return "redirect:/success"; // 회원가입 후 성공 페이지로 리디렉션
    }

    @PostMapping("/register")
    public String register(Member member) {
        // 일반 회원가입 처리 로직 추가
        memberManagementService.save(member);
        return "redirect:/success"; // 회원가입 후 성공 페이지로 리디렉션
    }

    @GetMapping("/success")
    public String success() {
        return "success"; // success.html을 반환 (성공 페이지 생성 필요)
    }
}
 
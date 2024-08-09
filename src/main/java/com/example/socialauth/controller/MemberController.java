package com.example.socialauth.controller;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberManagementService;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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

    @GetMapping("/")
    public String home() {
        return "index"; // index.html 반환
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 반환
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId, @RequestParam(required = false) String password, HttpSession session, Model model) {
        Optional<Member> optionalMember = memberManagementService.findByLoginId(loginId);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (member.getLoginType() == Member.LoginType.BASIC) {
                if (password != null && passwordEncoder.matches(password, member.getPassword())) {
                    session.setAttribute("member", member);
                    return "redirect:/success";
                } else {
                    model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                }
            } else {
                session.setAttribute("member", member);
                return "redirect:/success";
            }
        } else {
            model.addAttribute("error", "존재하지 않는 회원입니다.");
        }
        return "login";
    }

    @PostMapping({"/register", "/register_social"})
    public String register(Member member, HttpSession session, Model model) {
        String loginType = member.getLoginType().name();

        if (memberManagementService.findByLoginId(member.getLoginId()).isPresent()) {
            model.addAttribute("error", "이미 존재하는 회원입니다.");
            return "register";
        }

        if ("BASIC".equals(loginType)) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        } else {
            member.setPassword(null); // 소셜 로그인은 비밀번호 설정 안함
        }

        memberManagementService.save(member);

        // 소셜 로그인 정보 초기화
        if ("GOOGLE".equals(loginType) || "NAVER".equals(loginType)) {
            session.removeAttribute("userAttributes");
            session.removeAttribute("loginType");
        }

        return "redirect:/success";
    }

    @GetMapping("/success")
    public String success() {
        return "success"; // success.html 반환
    }

    @GetMapping("/register")
    public ModelAndView showRegisterForm(HttpSession session, ModelAndView mav, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        String loginType = (String) session.getAttribute("loginType");
        Map<String, Object> userAttributes = (Map<String, Object>) session.getAttribute("userAttributes");

        if (loginType != null && userAttributes != null) {
            // 소셜 회원가입 폼으로 이동
            mav.addObject("userAttributes", userAttributes);
            mav.addObject("isSocialLogin", true);
        } else {
            // 일반 회원가입 폼으로 이동
            mav.addObject("isSocialLogin", false);
            session.removeAttribute("userAttributes");
            session.removeAttribute("loginType");
        }

        mav.setViewName("register");
        return mav;
    }

}

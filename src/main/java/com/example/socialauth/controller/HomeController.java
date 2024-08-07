package com.example.socialauth.controller;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;

    @GetMapping("/register")
    public String showRegisterForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Map<String, Object> userAttributes = (Map<String, Object>) session.getAttribute("userAttributes");
        boolean isSocialLogin = userAttributes != null;
        model.addAttribute("isSocialLogin", isSocialLogin);
        model.addAttribute("userAttributes", userAttributes);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        String name = params.get("name");
        String nickname = params.get("nickname");
        String loginId = params.get("loginId");  // 로그인 ID는 이메일로 사용
        String email = params.get("email");
        String password = params.get("password");

        memberService.registerMember(name, nickname, loginId, email, password, Member.LoginType.Normal);

        redirectAttributes.addFlashAttribute("message", "Registration successful");
        return "redirect:/success";
    }

    @PostMapping("/register_social")
    public String registerSocial(@RequestParam Map<String, String> params, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String name = params.get("name");
        String nickname = params.get("nickname");
        String loginId = params.get("loginId");  // 로그인 ID는 이메일로 사용
        String email = params.get("email");

        // 세션에서 소셜 로그인 정보를 가져옴
        HttpSession session = request.getSession();
        Map<String, Object> userAttributes = (Map<String, Object>) session.getAttribute("userAttributes");

        if (userAttributes != null) {
            String loginTypeStr = (String) userAttributes.get("loginType");
            Member.LoginType loginType = Member.LoginType.valueOf(loginTypeStr);
            memberService.registerSocialMember(name, nickname, loginId, email, loginType);
        }

        redirectAttributes.addFlashAttribute("message", "Registration successful");
        return "redirect:/success";
    }
}

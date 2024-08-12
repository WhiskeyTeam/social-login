package com.example.socialauth.controller;

import com.example.socialauth.entity.LoginType;
import com.example.socialauth.entity.Member;
import com.example.socialauth.entity.Role;
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

@Controller
public class MemberController {

    private final SocialLoginService socialLoginService;
    private final MemberManagementService memberManagementService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberController(SocialLoginService socialLoginService, MemberManagementService memberManagementService, PasswordEncoder passwordEncoder) {
        this.socialLoginService = socialLoginService;
        this.memberManagementService = memberManagementService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "index"; // index.html 반환
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 반환
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam(required = false) String password,
                        HttpSession session,
                        Model model) {
        try {
            Member member = memberManagementService.findByLoginId(loginId);

            if (member.getLoginType() == LoginType.BASIC) {
                if (password != null && passwordEncoder.matches(password, member.getPassword())) {
                    session.setAttribute("member", member);
                    return "redirect:/success";
                } else {
                    model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                }
            } else {
                model.addAttribute("error", "소셜 로그인으로 가입된 계정입니다. 소셜 로그인 해주세요.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "존재하지 않는 회원입니다.");
        }

        return "login";
    }

    @PostMapping("/register_basic")
    public String registerBasic(@RequestParam Map<String, String> allParams, Model model) {
        String loginId = allParams.get("loginId");
        String password = allParams.get("password");
        String name = allParams.get("name");
        String email = allParams.get("email");
        String nickname = allParams.get("nickname");

        try {
            // Check if the loginId already exists
            memberManagementService.findByLoginId(loginId);
            model.addAttribute("error", "이미 존재하는 로그인 ID입니다. 다른 ID를 사용해주세요.");
            return "register_basic";
        } catch (Exception e) {
            // Proceed if member does not exist
        }

        // Encode the password
        String encodedPassword = passwordEncoder.encode(password);

        // Create a new member
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(encodedPassword);
        member.setName(name);
        member.setEmail(email);
        member.setNickname(nickname);
        member.setLoginType(LoginType.BASIC);
        member.setRole(Role.USER);

        try {
            memberManagementService.save(member);
        } catch (Exception ex) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "register_basic";
        }

        return "redirect:/login";
    }

    @PostMapping("/register_social")
    public String registerSocial(@RequestParam String nickname, HttpSession session, Model model) {
        String loginId = (String) session.getAttribute("loginId");
        String loginTypeStr = (String) session.getAttribute("loginType");

        if (loginId == null || loginTypeStr == null) {
            return "redirect:/login"; // Redirect to login page if session data is missing
        }

        LoginType loginType = LoginType.valueOf(loginTypeStr);

        try {
            // Check if member already exists
            Member existingMember = socialLoginService.findMemberByLoginIdAndLoginType(loginId, loginType);
            if (existingMember != null) {
                model.addAttribute("error", "이미 존재하는 회원입니다.");
                return "register_social";
            }

            // Create a new social member
            Member member = new Member();
            member.setLoginId(loginId);
            member.setNickname(nickname);
            member.setLoginType(loginType);
            member.setRole(Role.USER);
            member.setPassword(null); // No password needed for social login

            socialLoginService.save(member);
            session.setAttribute("member", member);
            return "redirect:/success";
        } catch (Exception ex) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "register_social";
        }
    }

    @GetMapping("/success")
    public String success() {
        return "success"; // success.html 반환
    }

    @GetMapping("/register_basic")
    public String showBasicRegisterForm(Model model) {
        model.addAttribute("isSocialLogin", false);
        return "register_basic";
    }

    @GetMapping("/register_social")
    public String showSocialRegisterForm(HttpSession session, Model model) {
        String loginType = (String) session.getAttribute("loginType");
        Map<String, Object> userAttributes = (Map<String, Object>) session.getAttribute("userAttributes");

        if (loginType != null && userAttributes != null) {
            model.addAttribute("userAttributes", userAttributes);
            model.addAttribute("isSocialLogin", true);
            return "register_social";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate the session completely
        return "redirect:/login";
    }
}

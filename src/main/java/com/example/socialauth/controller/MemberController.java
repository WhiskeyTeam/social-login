package com.example.socialauth.controller;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberManagementService;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

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
        System.out.println("Login attempt with ID: " + loginId);

        try {
            Member member = memberManagementService.findByLoginId(loginId);
            System.out.println("Member found: " + member.getLoginId());
            System.out.println("Stored password: " + member.getPassword()); // 인코딩된 비밀번호 출력
            System.out.println("Provided password: " + password); // 입력한 비밀번호 출력

            if (member.getLoginType() == Member.LoginType.BASIC) {
                if (password != null && passwordEncoder.matches(password, member.getPassword())) {
                    session.setAttribute("member", member);
                    System.out.println("Password matches!");
                    return "redirect:/success";
                } else {
                    System.out.println("Password does not match!");
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
            Member existingMember = memberManagementService.findByLoginId(loginId);
            model.addAttribute("error", "이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
            return "register_basic"; // 회원가입 페이지로 다시 돌아감
        } catch (Exception e) {
            // 회원이 존재하지 않을 경우 계속 진행
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("Encoded password: " + encodedPassword); // 인코딩된 비밀번호 출력

        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(encodedPassword); // 인코딩된 비밀번호 저장
        member.setName(name);
        member.setEmail(email);
        member.setNickname(nickname);
        member.setLoginType(Member.LoginType.BASIC);
        member.setRole(Member.Role.USER);

        try {
            memberManagementService.save(member);
            System.out.println("Member saved successfully: " + member.getLoginId());
        } catch (Exception ex) {
            System.err.println("Error saving member: " + ex.getMessage());
            model.addAttribute("error", "이미 존재하는 로그인 ID입니다. 다른 ID를 사용해주세요.");
            return "register_basic";
        }

        return "redirect:/login";
    }

    @PostMapping("/checkLoginId")
    public ResponseEntity<Map<String, Boolean>> checkLoginId(@RequestParam String loginId) {
        try {
            Member existingMember = memberManagementService.findByLoginId(loginId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/register_social")
    public String registerSocial(Member member, HttpSession session, Model model) {
        String loginId = (String) session.getAttribute("loginId");
        Member.LoginType loginType = Member.LoginType.valueOf((String) session.getAttribute("loginType"));

        try {
            Member existingMember = socialLoginService.findMemberByLoginIdAndLoginType(loginId, loginType);
            model.addAttribute("error", "이미 존재하는 회원입니다.");
            return "register_social";
        } catch (Exception e) {
            // 회원이 존재하지 않을 경우 계속 진행
        }

        member.setLoginId(loginId);
        member.setLoginType(loginType);
        member.setRole(Member.Role.USER);
        member.setPassword(null); // 소셜 로그인은 비밀번호가 필요 없음
        socialLoginService.save(member);

        session.setAttribute("member", member);
        return "redirect:/success";
    }

    @GetMapping("/success")
    public String success() {
        System.out.println("Success page requested");
        return "success"; // success.html 반환
    }

    @GetMapping("/register_basic")
    public String showBasicRegisterForm(HttpSession session, Model model) {
        // 세션에서 소셜 로그인 정보 제거
        session.removeAttribute("userAttributes");
        session.removeAttribute("isSocialLogin");
        session.removeAttribute("loginType");

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
        session.invalidate(); // 세션 완전히 초기화
        return "redirect:/login";
    }
}

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

    @PostMapping("/register_basic")
    public String registerBasic(@RequestParam Map<String, String> allParams, Model model) {
        String loginId = allParams.get("loginId");
        String password = allParams.get("password");
        String name = allParams.get("name");
        String email = allParams.get("email");
        String nickname = allParams.get("nickname");

        // 디버깅용 로그
        System.out.println("Received Params: " + allParams.toString());

        // loginId(이메일)가 이미 존재하는지 체크
        Optional<Member> existingMember = memberManagementService.findByLoginId(loginId);

        if (existingMember.isPresent()) {
            // 이미 존재하는 loginId(이메일)이면 에러 메시지 추가
            model.addAttribute("error", "이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
            return "register_basic"; // 회원가입 페이지로 다시 돌아감
        }

        // loginId(이메일)가 존재하지 않는 경우 회원가입 진행
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setEmail(email);
        member.setNickname(nickname);
        member.setLoginType(Member.LoginType.BASIC);
        member.setRole(Member.Role.USER);

        try {
            memberManagementService.save(member);
            System.out.println("Member saved successfully: " + member.getLoginId());
        } catch (Exception e) {
            System.err.println("Error saving member: " + e.getMessage());
            model.addAttribute("error", "회원가입 중 문제가 발생했습니다. 다시 시도해주세요.");
            return "register_basic";
        }

        return "redirect:/success"; // 회원가입 성공 시 리다이렉트
    }


    @PostMapping("/checkLoginId")
    public ResponseEntity<Map<String, Boolean>> checkLoginId(@RequestParam String loginId) {
        Optional<Member> existingMember = memberManagementService.findByLoginId(loginId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", existingMember.isPresent());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register_social")
    public String registerSocial(Member member, HttpSession session, Model model) {
        String loginId = (String) session.getAttribute("loginId");
        Member.LoginType loginType = Member.LoginType.valueOf((String) session.getAttribute("loginType"));

        if (socialLoginService.findMemberByLoginIdAndLoginType(loginId, loginType).isPresent()) {
            model.addAttribute("error", "이미 존재하는 회원입니다.");
            return "register_social";
        }

        member.setLoginId(loginId);
        member.setLoginType(loginType);
        member.setRole(Member.Role.USER);
        member.setPassword(null);
        socialLoginService.save(member);

        session.setAttribute("member", member);
        return "redirect:/success";
    }



    @GetMapping("/success")
    public String success() {
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

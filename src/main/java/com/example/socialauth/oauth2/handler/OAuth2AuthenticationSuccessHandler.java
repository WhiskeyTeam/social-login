package com.example.socialauth.oauth2.handler;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;

    public OAuth2AuthenticationSuccessHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> userAttributes = oAuth2User.getAttributes();
        String email = (String) userAttributes.get("email");
        String loginId;

        // 네이버와 구글의 사용자 식별자 처리
        String registrationId = getClientRegistrationId(authentication);
        if ("google".equals(registrationId)) {
            loginId = (String) userAttributes.get("sub");
            userAttributes.put("loginType", Member.LoginType.Google.name());
        } else if ("naver".equals(registrationId)) {
            loginId = (String) userAttributes.get("id");
            userAttributes.put("loginType", Member.LoginType.Naver.name());
        } else {
            // 기타 소셜 로그인 타입에 대한 처리 (필요시 추가)
            loginId = email;
            userAttributes.put("loginType", Member.LoginType.Normal.name());
        }

        // 로그인 ID는 이메일로 사용
        Member existingMember = memberService.findByLoginId(loginId);

        if (existingMember != null) {
            // 이미 가입된 회원인 경우
            getRedirectStrategy().sendRedirect(request, response, "/success");
        } else {
            // 가입되지 않은 회원인 경우
            userAttributes.put("loginId", loginId);
            request.getSession().setAttribute("userAttributes", userAttributes);
            getRedirectStrategy().sendRedirect(request, response, "/register");
        }
    }

    private String getClientRegistrationId(Authentication authentication) {
        return ((OAuth2User) authentication.getPrincipal()).getAttributes()
                .get("clientRegistrationId")
                .toString();
    }
}

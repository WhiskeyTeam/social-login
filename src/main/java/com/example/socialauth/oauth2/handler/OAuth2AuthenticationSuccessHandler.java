package com.example.socialauth.oauth2.handler;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialLoginService socialLoginService;

    public OAuth2AuthenticationSuccessHandler(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
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

        // 세션에 소셜 로그인 사용자 정보를 저장
        request.getSession().setAttribute("userAttributes", userAttributes);

        // 회원가입 페이지로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, "/register");
    }

    private String getClientRegistrationId(Authentication authentication) {
        return ((OAuth2User) authentication.getPrincipal()).getAttributes()
                .get("clientRegistrationId")
                .toString();
    }
}

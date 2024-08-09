package com.example.socialauth.oauth2.handler;

import com.example.socialauth.entity.Member;
import com.example.socialauth.service.SocialLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 로그인 성공 시 처리하는 핸들러 클래스입니다.
 */
@Component
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
        String loginId = null;
        Member.LoginType loginType;

        // 소셜 로그인 타입을 결정하고 null이 될 수 없도록 보장
        String registrationId = getClientRegistrationId(authentication);


        switch (registrationId) {
            case "google":
                loginId = (String) userAttributes.get("sub");
                loginType = Member.LoginType.Google;
                break;
            case "naver":
                loginId = (String) userAttributes.get("id");
                loginType = Member.LoginType.Naver;
                break;
            default:
                throw new IllegalStateException("Unknown registrationId: " + registrationId);
        }

        Optional<Member> optionalMember = socialLoginService.findMemberByLoginIdAndLoginType(loginId, loginType);

        if (optionalMember.isPresent()) {
            socialLoginService.handleSocialLogin(request.getSession(), loginId, (String) userAttributes.get("name"), email, loginType.name());
            getRedirectStrategy().sendRedirect(request, response, "/success");
        } else {
            // 사용자가 존재하지 않으면 회원가입 페이지로 리디렉션
            request.getSession().setAttribute("userAttributes", userAttributes);
            request.getSession().setAttribute("isSocialLogin", true);
            getRedirectStrategy().sendRedirect(request, response, "/register");
        }
    }

    private String getClientRegistrationId(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        System.out.println("OAuth2User attributes: " + attributes);

        if (attributes.containsKey("sub")) {
            return "google";
        } else if (attributes.containsKey("id")) {
            return "naver";
        }
        throw new IllegalStateException("Unknown OAuth2 provider");
    }
}

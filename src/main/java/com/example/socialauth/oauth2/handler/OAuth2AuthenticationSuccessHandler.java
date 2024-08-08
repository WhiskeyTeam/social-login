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
        String loginType = null;

        String registrationId = getClientRegistrationId(authentication);
        if ("google".equals(registrationId)) {
            loginId = (String) userAttributes.get("sub");
            loginType = "Google";
            userAttributes.put("loginType", Member.LoginType.Google.name());
        } else if ("naver".equals(registrationId)) {
            loginId = (String) userAttributes.get("id");
            loginType = "Naver";
            userAttributes.put("loginType", Member.LoginType.Naver.name());
        }

        socialLoginService.handleSocialLogin(request.getSession(), loginId, (String) userAttributes.get("name"), email, loginType);

        Optional<Member> optionalMember = Optional.empty();
        if ("Google".equalsIgnoreCase(loginType)) {
            optionalMember = socialLoginService.findMemberByGoogleSub(loginId);
        } else if ("Naver".equalsIgnoreCase(loginType)) {
            optionalMember = socialLoginService.findMemberByNaverId(loginId);
        }

        if (optionalMember.isPresent()) {
            getRedirectStrategy().sendRedirect(request, response, "/home");
        } else {
            request.getSession().setAttribute("userAttributes", userAttributes);
            request.getSession().setAttribute("isSocialLogin", true);
            getRedirectStrategy().sendRedirect(request, response, "/register");
        }
    }

    private String getClientRegistrationId(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        return oAuth2User.getAuthorities().iterator().next().getAuthority();
    }
}

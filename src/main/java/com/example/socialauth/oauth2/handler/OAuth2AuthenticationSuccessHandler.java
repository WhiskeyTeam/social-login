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

    /**
     * OAuth2 로그인 성공 시 호출되는 메서드입니다.
     * 사용자 정보를 통해 회원 가입 또는 로그인 처리를 합니다.
     *
     * @param request        HttpServletRequest 객체
     * @param response       HttpServletResponse 객체
     * @param authentication 인증 정보 객체
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 인증된 사용자의 OAuth2User 객체를 가져옵니다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> userAttributes = oAuth2User.getAttributes();

        // 사용자 이메일을 가져옵니다.
        String email = (String) userAttributes.get("email");
        String loginId = null;
        String loginType = null;

        // 클라이언트 등록 ID를 가져옵니다.
        String registrationId = getClientRegistrationId(authentication);
        if ("google".equals(registrationId)) {
            // 구글 로그인인 경우
            loginId = (String) userAttributes.get("sub");
            loginType = "Google";
        } else if ("naver".equals(registrationId)) {
            // 네이버 로그인인 경우
            loginId = (String) userAttributes.get("id");
            loginType = "Naver";
        }

        // 소셜 로그인 처리 메서드 호출
        socialLoginService.handleSocialLogin(request.getSession(), loginId, (String) userAttributes.get("name"), email, loginType);

        // 사용자 존재 여부 확인
        Optional<Member> optionalMember = Optional.empty();
        if ("Google".equalsIgnoreCase(loginType)) {
            optionalMember = socialLoginService.findMemberByGoogleSub(loginId);
        } else if ("Naver".equalsIgnoreCase(loginType)) {
            optionalMember = socialLoginService.findMemberByNaverId(loginId);
        }

        // 사용자 존재 여부에 따라 리디렉션 처리
        if (optionalMember.isPresent()) {
            // 이미 존재하는 회원인 경우
            getRedirectStrategy().sendRedirect(request, response, "/success");
        } else {
            // 존재하지 않는 회원인 경우 회원가입 페이지로 리디렉션
            request.getSession().setAttribute("userAttributes", userAttributes);
            request.getSession().setAttribute("isSocialLogin", true);
            getRedirectStrategy().sendRedirect(request, response, "/register");
        }
    }

    /**
     * 인증된 사용자의 클라이언트 등록 ID를 반환합니다.
     * 구글과 네이버를 구분하기 위해 사용됩니다.
     *
     * @param authentication 인증 정보 객체
     * @return 클라이언트 등록 ID
     */
    private String getClientRegistrationId(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("iss")) {
            // 구글의 경우 "iss" 키가 포함되어 있음
            return "google";
        } else if (attributes.containsKey("response")) {
            // 네이버의 경우 "response" 키가 포함되어 있음
            return "naver";
        }
        return null;
    }
}

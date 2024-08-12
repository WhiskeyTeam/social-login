package com.example.socialauth.service;

import com.example.socialauth.entity.Member;
import com.example.socialauth.oauth2.CustomOAuth2User;
import com.example.socialauth.oauth2.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2AuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final SocialLoginService socialLoginService;
    private final HttpSession session;
    private final HttpServletResponse response;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        log.info("CustomOAuth2AuthService");

        OAuth2User oAuth2User = null;

        try {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            oAuth2User = delegate.loadUser(request);

            if (oAuth2User == null) {
                log.warn("OAuth2User is null, redirecting to social registration page.");
                redirectToRegisterPage();
                return null;
            }

            String registrationId = request.getClientRegistration().getRegistrationId();
            String userNameAttributeName = request.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

            Member member = socialLoginService.findMemberByLoginIdAndLoginType(attributes.getOauthId(), attributes.getLoginType());

            if (member != null) {
                // 사용자가 존재하는 경우 로그인
                return new CustomOAuth2User(attributes.getAttributes(), attributes.getNameAttributeKey());
            } else {
                // 사용자가 존재하지 않는 경우, 회원가입 페이지로 리다이렉트
                session.setAttribute("userAttributes", attributes.getAttributes());
                session.setAttribute("loginType", attributes.getLoginType().toString());
                session.setAttribute("loginId", attributes.getOauthId());
                redirectToRegisterPage();
                return null;
            }

        } catch (Exception e) {
            log.error("Exception during OAuth2 authentication: ", e);
            try {
                redirectToErrorPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }
    }

    private void redirectToRegisterPage() throws IOException {
        response.sendRedirect("/register_social");
    }

    private void redirectToErrorPage() throws IOException {
        response.sendRedirect("/error");
    }
}

package com.example.socialauth.service;

import com.example.socialauth.oauth2.OAuth2Attributes;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2AuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2AuthService");

        try {
            // DefaultOAuth2UserService를 사용하여 OAuth2User를 로드
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(request);

            // access_token을 로그로 출력
            String accessToken = request.getAccessToken().getTokenValue();
            log.info("Access Token: {}", accessToken);

            // 클라이언트 등록 ID를 가져옴
            String registrationId = request.getClientRegistration().getRegistrationId();

            // 사용자 이름 속성 이름을 가져옴
            String userNameAttributeName = request.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            // OAuth2Attributes를 생성하여 사용자 속성 맵핑
            OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

            // DefaultOAuth2User를 생성하여 반환
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        } catch (OAuth2AuthenticationException e) {
            // OAuth2 인증 예외 처리
            log.error("OAuth2AuthenticationException: ", e);
            throw e;
        } catch (Exception e) {
            // 일반 예외 처리
            log.error("Exception: ", e);
            OAuth2Error oauth2Error = new OAuth2Error("server_error", "Failed to load user", null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }

}

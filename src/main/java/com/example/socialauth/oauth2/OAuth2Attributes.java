package com.example.socialauth.oauth2;

import com.example.socialauth.oauth2.exception.OAuth2RegistrationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@Getter
public class OAuth2Attributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String oauthId;
    private final String nickname;
    private final String email;
    private final String picture;
    private final Provider provider;

    @Builder
    public OAuth2Attributes(Map<String, Object> attributes, String nameAttributeKey, String oauthId, String nickname, String email, String picture, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
    }

    @SneakyThrows
    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        log.info("userNameAttributeName = {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userNameAttributeName));
        log.info("attributes = {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(attributes));
        String registrationIdToLower = registrationId.toLowerCase();
        switch (registrationIdToLower) {
            case "naver":
                return ofNaver(userNameAttributeName, attributes);
            default:
                throw new OAuth2RegistrationException("해당 소셜 로그인은 현재 지원하지 않습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Attributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuth2Attributes.builder()
                .oauthId((String) response.get("id"))
                .nickname((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .provider(Provider.NAVER)
                .attributes(response)
                .nameAttributeKey("id")
                .build();
    }
}


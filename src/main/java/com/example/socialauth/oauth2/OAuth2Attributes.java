package com.example.socialauth.oauth2;

import com.example.socialauth.entity.Member;
import com.example.socialauth.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
    public OAuth2Attributes(Map<String, Object> attributes, String nameAttributeKey, String oauthId,
                            String nickname, String email, String picture, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
    }

    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) throws BadRequestException {
        try {
            log.info("userNameAttributeName = {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userNameAttributeName));
            log.info("attributes = {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(attributes));
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON for logging", e);
            throw new BadRequestException("Error processing JSON for logging");
        }

        String registrationIdToLower = registrationId.toLowerCase();

        switch (registrationIdToLower) {
            case "naver":
                return ofNaver(userNameAttributeName, attributes);

            case "google":
                return ofGoogle(userNameAttributeName, attributes);

            default:
                throw new BadRequestException("해당 소셜 로그인은 현재 지원하지 않습니다.");
        }
    }

    private static OAuth2Attributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attributes.builder()
                .oauthId((String) response.get("id"))
                .nickname((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .provider(Provider.Naver)
                .attributes(response)
                .nameAttributeKey("id")
                .build();
    }

    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .oauthId((String) attributes.get(userNameAttributeName))
                .nickname((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .provider(Provider.Google)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public String getLoginId() {
        return this.oauthId; // oauthId를 loginId로 사용
    }

    public Member.LoginType getLoginType() {
        return this.provider.toLoginType(); // Provider에 따라 로그인 타입 결정
    }

    public String getName() {
        return this.nickname; // OAuth2Attributes에서 닉네임을 이름으로 사용
    }
}

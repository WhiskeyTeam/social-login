package com.example.socialauth.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(Map<String, Object> attributes, String nameAttributeKey) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return (String) attributes.get(nameAttributeKey);
    }
}

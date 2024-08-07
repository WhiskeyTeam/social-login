package com.example.socialauth.config;

import com.example.socialauth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.example.socialauth.service.SocialLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class OAuth2SuccessHandlerConfig {

    private SocialLoginService socialLoginService;

    @Autowired
    public void setSocialLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(socialLoginService);
    }
}

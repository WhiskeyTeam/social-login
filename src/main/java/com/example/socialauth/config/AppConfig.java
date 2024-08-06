package com.example.socialauth.config;

import com.example.socialauth.handler.CustomLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("DXEMF06fL20Py67dy9pp")
    private String naverClientId;

    @Value("89Y9atEqvW")
    private String naverClientSecret;

    @Value("62790710914-0uj632naot18b9s7vq28inn1122l506o.apps.googleusercontent.com")
    private String googleClientId;

    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler(naverClientId, naverClientSecret, googleClientId);
    }
}

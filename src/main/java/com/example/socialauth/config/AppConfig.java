package com.example.socialauth.config;

import com.example.socialauth.handler.CustomLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("DXEMF06fL20Py67dy9pp")
    private String clientId;

    @Value("89Y9atEqvW")
    private String clientSecret;

    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler(clientId, clientSecret);
    }
}

package com.example.socialauth.config;

import com.example.socialauth.handler.CustomLogoutSuccessHandler;
import com.example.socialauth.handler.OAuth2AuthenticationFailureHandler;
import com.example.socialauth.handler.OAuth2AuthenticationSuccessHandler;
import com.example.socialauth.service.CustomOAuth2AuthService;
import com.example.socialauth.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private CustomOAuth2AuthService customOAuth2AuthService;
    private CustomOidcUserService customOidcUserService;
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Autowired
    public void setCustomOAuth2AuthService(CustomOAuth2AuthService customOAuth2AuthService) {
        this.customOAuth2AuthService = customOAuth2AuthService;
    }

    @Autowired
    public void setCustomOidcUserService(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Autowired
    public void setOAuth2AuthenticationFailureHandler(OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }

    @Autowired
    public void setOAuth2AuthenticationSuccessHandler(OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Autowired
    public void setCustomLogoutSuccessHandler(CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register_basic", "/register_social", "/success", "/css/**", "/js/**", "/images/**").permitAll() // 여기 추가
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2AuthService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.sendRedirect("/home");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

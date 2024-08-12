package com.example.socialauth.config;

import com.example.socialauth.handler.CustomLogoutSuccessHandler;
import com.example.socialauth.handler.OAuth2AuthenticationFailureHandler;
import com.example.socialauth.handler.OAuth2AuthenticationSuccessHandler;
import com.example.socialauth.service.CustomOAuth2AuthService;
import com.example.socialauth.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
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

    private final CustomOAuth2AuthService customOAuth2AuthService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    // Constructor-based dependency injection
    public SecurityConfig(CustomOAuth2AuthService customOAuth2AuthService,
                          CustomOidcUserService customOidcUserService,
                          OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                          CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.customOAuth2AuthService = customOAuth2AuthService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register_basic", "/register_social", "/success", "/css/**", "/js/**", "/images/**")
                        .permitAll()
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
    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception.getMessage().equals("User registration required")) {
                response.sendRedirect("/register_social"); // 회원가입 페이지로 리다이렉트
            } else {
                response.sendRedirect("/login?error=true"); // 일반적인 오류 처리
            }
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

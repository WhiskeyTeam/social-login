package com.example.socialauth.config;

import com.example.socialauth.handler.CustomLogoutSuccessHandler;
import com.example.socialauth.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.example.socialauth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.example.socialauth.oauth2.service.CustomOAuth2AuthService;
import com.example.socialauth.oauth2.service.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2AuthService customOAuth2AuthService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    /**
     * 비밀번호 인코더를 빈으로 등록합니다.
     *
     * @return PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain을 구성하여 HTTP 보안을 설정합니다.
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()  // HTTP 기본 인증 비활성화
                .formLogin(form -> form
                        .loginPage("/login")  // 사용자 정의 로그인 페이지
                        .defaultSuccessUrl("/success", true)  // 로그인 성공 시 리다이렉트 URL
                        .permitAll()
                )
                .csrf().disable()  // CSRF 보호 비활성화
                .cors().and()  // CORS 활성화
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션 정책을 Stateless로 설정
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/success", "/images/**", "/js/**", "/webjars/**").permitAll()  // 특정 경로에 대해 인증 없이 접근 허용
                        .anyRequest().authenticated()  // 나머지 요청에 대해 인증 요구
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")  // 사용자 정의 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)  // OIDC 사용자 서비스 설정
                                .userService(customOAuth2AuthService)  // OAuth2 사용자 서비스 설정
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)  // 인증 성공 핸들러
                        .failureHandler(oAuth2AuthenticationFailureHandler)  // 인증 실패 핸들러
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 URL 설정
                        .logoutSuccessHandler(customLogoutSuccessHandler)  // 로그아웃 성공 핸들러 설정
                        .permitAll()
                );

        return http.build();
    }
}

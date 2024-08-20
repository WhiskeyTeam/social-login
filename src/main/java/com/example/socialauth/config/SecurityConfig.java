package com.example.socialauth.config;

import com.example.socialauth.service.CustomOAuth2AuthService;
import com.example.socialauth.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2AuthService customOAuth2AuthService;
    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOAuth2AuthService customOAuth2AuthService,
                          CustomOidcUserService customOidcUserService) {
        this.customOAuth2AuthService = customOAuth2AuthService;
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                ) // 세션 관리 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 그 외의 모든 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/mainPage", true)
                        .failureUrl("/login?error=true")
                        .successHandler((request, response, authentication) -> {
                            log.info("User {} has successfully logged in.", authentication.getName());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.info("User Roles after setting context: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());

                            // 세션에 인증 정보 추가
                            request.getSession().setAttribute("isAuthenticated", true);

                            // userRole을 문자열로 변환하여 세션에 저장
                            String userRole = authentication.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.joining(","));
                            request.getSession().setAttribute("userRole", userRole);

                            if (!response.isCommitted()) {
                                response.sendRedirect("/mainPage");
                            } else {
                                log.warn("Response already committed. Unable to redirect.");
                            }
                        })
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 사용자 정의 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2AuthService) // OAuth2 사용자 서비스 설정
                                .oidcUserService(customOidcUserService) // OIDC 사용자 서비스 설정
                        )
                        .defaultSuccessUrl("/mainPage", true) // OAuth2 로그인 성공 후 이동할 기본 페이지
                        .failureUrl("/login?error=true") // OAuth2 로그인 실패 시 이동할 페이지
                        .successHandler((request, response, authentication) -> {
                            log.info("User {} has successfully logged in via OAuth2.", authentication.getName());

                            // 세션에 인증 정보 추가
                            request.getSession().setAttribute("isAuthenticated", true);

                            // userRole을 문자열로 변환하여 세션에 저장
                            String userRole = authentication.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.joining(","));
                            request.getSession().setAttribute("userRole", userRole);

                            if (!response.isCommitted()) {
                                response.sendRedirect("/mainPage");
                            } else {
                                log.warn("Response already committed. Unable to redirect.");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL 설정
                        .logoutSuccessUrl("/mainPage") // 로그아웃 성공 후 이동할 페이지
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll() // 로그아웃 관련 요청은 모두 허용
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://web.dokalab.site"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 인코더 빈 등록
    }
}

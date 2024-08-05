package com.example.socialauth.config;

import com.example.socialauth.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.example.socialauth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.example.socialauth.oauth2.service.CustomOAuth2AuthService;
import com.example.socialauth.oauth2.service.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2AuthService customOAuth2AuthService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

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
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(clientId, clientSecret))  // 로그아웃 성공 핸들러 설정
                        .permitAll()
                );

        return http.build();
    }

    /**
     * CustomLogoutSuccessHandler는 사용자가 로그아웃할 때 추가 처리를 수행합니다.
     */
    public static class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

        private final String clientId;
        private final String clientSecret;

        /**
         * CustomLogoutSuccessHandler 생성자
         *
         * @param clientId     클라이언트 ID
         * @param clientSecret 클라이언트 시크릿
         */
        public CustomLogoutSuccessHandler(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        /**
         * 로그아웃 성공 시 호출되는 메서드
         *
         * @param request      HttpServletRequest 객체
         * @param response     HttpServletResponse 객체
         * @param authentication 인증 정보
         * @throws IOException 예외 발생 시
         * @throws ServletException 예외 발생 시
         */
        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                String accessToken = oauthToken.getPrincipal().getAttribute("access_token");

                if (accessToken != null) {
                    // 네이버 토큰 무효화 요청
                    String naverRevokeTokenUrl = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token")
                            .queryParam("grant_type", "delete")
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("access_token", accessToken)
                            .queryParam("service_provider", "NAVER")
                            .toUriString();

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getForObject(naverRevokeTokenUrl, String.class);
                }
            }

            // 세션 무효화
            request.getSession().invalidate();

            // 브라우저 쿠키 삭제
            for (Cookie cookie : request.getCookies()) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
            // 네이버 로그인 페이지로 리디렉션하여 새로운 계정으로 로그인 가능하도록 함
            response.sendRedirect("https://nid.naver.com/nidlogin.login");
        }
    }
}

package com.example.socialauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String clientId;
    private final String clientSecret;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 세션에서 accessToken을 가져옴
        String accessToken = (String) request.getSession().getAttribute("accessToken");

        if (accessToken != null) {
            // 네이버 API를 통해 토큰 무효화
            invalidateNaverToken(accessToken);

            // 세션에서 accessToken을 삭제
            request.getSession().removeAttribute("accessToken");
        }

        // 세션 무효화
        request.getSession().invalidate();

        // 로그아웃 후 로그인 페이지로 리다이렉트
        response.sendRedirect("/login");
    }

    /**
     * 네이버 API를 통해 토큰을 무효화하는 메서드
     * @param accessToken 무효화할 네이버 액세스 토큰
     */
    private void invalidateNaverToken(String accessToken) {
        // 네이버 API 요청 URL 생성
        String url = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "delete")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("access_token", accessToken)
                .queryParam("service_provider", "NAVER")
                .toUriString();

        // RestTemplate을 사용하여 네이버 API에 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(url, String.class);
    }
}

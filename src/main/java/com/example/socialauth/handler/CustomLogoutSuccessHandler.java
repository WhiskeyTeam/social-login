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

    private final String naverClientId;
    private final String naverClientSecret;
    private final String googleClientId;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 세션에서 accessToken과 provider를 가져옴
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        String provider = (String) request.getSession().getAttribute("provider");

        if (accessToken != null && provider != null) {
            // 각 소셜 로그인 제공자에 따라 토큰 무효화
            switch (provider.toLowerCase()) {
                case "naver":
                    invalidateNaverToken(accessToken);
                    break;
                case "google":
                    invalidateGoogleToken(accessToken);
                    break;
            }

            // 세션에서 accessToken과 provider를 삭제
            request.getSession().removeAttribute("accessToken");
            request.getSession().removeAttribute("provider");
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
                .queryParam("client_id", naverClientId)
                .queryParam("client_secret", naverClientSecret)
                .queryParam("access_token", accessToken)
                .queryParam("service_provider", "NAVER")
                .toUriString();

        // RestTemplate을 사용하여 네이버 API에 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(url, String.class);
    }

    /**
     * 구글 API를 통해 토큰을 무효화하는 메서드
     * @param accessToken 무효화할 구글 액세스 토큰
     */
    private void invalidateGoogleToken(String accessToken) {
        // 구글 API 요청 URL 생성
        String url = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com/revoke")
                .queryParam("token", accessToken)
                .toUriString();

        // RestTemplate을 사용하여 구글 API에 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(url, null, String.class);
    }
}

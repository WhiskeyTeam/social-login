package com.example.socialauth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class VerificationService {

    private final RestTemplate restTemplate;

    @Value("http://web.dokalab.site:8084/api/redis")
    private String apiUrl;

    public VerificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 인증 코드 생성 및 저장
    public String generateAndSaveVerificationCode(String email) {
        String verificationCode = generateRandomCode(); // 난수 생성

        // API 요청 데이터 생성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", email);
        requestBody.put("value", verificationCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // API 요청
        restTemplate.postForObject(apiUrl, request, Void.class);

        return verificationCode;
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String inputCode) {
        String url = apiUrl + "?key=" + email;
        String storedCode = restTemplate.getForObject(url, String.class);

        return storedCode != null && storedCode.equals(inputCode);
    }

    // 난수 생성
    private String generateRandomCode() {
        int codeLength = 6; // 예: 6자리 코드
        Random random = new Random();
        StringBuilder code = new StringBuilder(codeLength);

        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10)); // 0-9 사이의 숫자 난수 생성
        }

        return code.toString();
    }
}

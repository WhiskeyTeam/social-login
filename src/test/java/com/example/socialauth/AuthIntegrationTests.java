package com.example.socialauth;

import com.example.socialauth.repository.MemberRepository;
import com.example.socialauth.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @WithMockUser(username = "googleuser", roles = {"USER"})
    public void testGoogleSocialLoginAndRegister() throws Exception {
        // Google 소셜 로그인 시뮬레이션
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());

        // Google 소셜 로그인 성공 후 콜백 처리
        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "google-test-code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/success"));

        // 데이터베이스에 Google 소셜 계정의 sub 값으로 회원가입된 사용자가 존재하는지 확인
        Optional<Member> member = memberRepository.findByLoginId("google-sub-id");
        assertThat(member.isPresent()).isTrue();
    }

    @Test
    @WithMockUser(username = "naveruser", roles = {"USER"})
    public void testNaverSocialLoginAndRegister() throws Exception {
        // Naver 소셜 로그인 시뮬레이션
        mockMvc.perform(get("/oauth2/authorization/naver"))
                .andExpect(status().is3xxRedirection());

        // Naver 소셜 로그인 성공 후 콜백 처리
        mockMvc.perform(get("/login/oauth2/code/naver")
                        .param("code", "naver-test-code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/success"));

        // 데이터베이스에 Naver 소셜 계정의 id 값으로 회원가입된 사용자가 존재하는지 확인
        Optional<Member> member = memberRepository.findByLoginId("naver-id");
        assertThat(member.isPresent()).isTrue();
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        String email = "test@example.com";
        String password = "password";
        String userId = "test@example.com";

        // 일반 회원가입 테스트
        mockMvc.perform(post("/register_basic")
                        .param("email", email)
                        .param("password", password)
                        .param("confirmPassword", password)
                        .param("userId", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // 일반 로그인 테스트
        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/success"));

        // 데이터베이스에 일반 계정의 식별자로 사용자가 존재하는지 확인
        Optional<Member> member = memberRepository.findByLoginId(userId);
        assertThat(member.isPresent()).isTrue();
    }
}

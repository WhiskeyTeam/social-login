package com.example.socialauth;

import com.example.socialauth.entity.LoginType;
import com.example.socialauth.entity.Member;
import com.example.socialauth.service.MemberManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MemberRegistrationTest {

    @Autowired
    private MemberManagementService memberManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void 회원가입_성공_테스트() {
        // 회원가입을 위한 데이터 설정
        String name = "TestUser";
        String nickname = "TestNick";
        String loginId = "testUser@example.com";
        String email = "testUser@example.com";
        String rawPassword = "testPassword";  // 원본 비밀번호
        LoginType loginType = LoginType.BASIC;

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 회원가입 호출 (비밀번호는 인코딩된 상태로 전달)
        Member savedMember = memberManagementService.registerMember(name, nickname, loginId, email, encodedPassword, loginType);

        // 결과 검증
        assertNotNull(savedMember, "회원가입이 정상적으로 이루어져야 합니다.");
        assertEquals(loginId, savedMember.getLoginId(), "저장된 회원의 로그인 ID가 일치해야 합니다.");
        assertEquals(nickname, savedMember.getNickname(), "닉네임은 인코딩되지 않고 원본 그대로 저장되어야 합니다.");
        assertTrue(passwordEncoder.matches(rawPassword, savedMember.getPassword()), "비밀번호가 인코딩되어 저장되고, 원본 비밀번호와 일치해야 합니다.");
    }

    @Test
    public void 회원가입_실패_테스트_중복_아이디() {
        // 중복된 아이디로 회원가입 시도
        String name = "TestUser";
        String nickname = "TestNick";
        String loginId = "testUser@example.com";
        String email = "testUser@example.com";
        String rawPassword = "testPassword";
        LoginType loginType = LoginType.BASIC;

        // 비밀번호를 인코딩한 후 회원가입 호출
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 중복된 아이디로 회원가입 시도
        assertThrows(IllegalArgumentException.class, () -> {
            memberManagementService.registerMember(name, nickname, loginId, email, encodedPassword, loginType);
        }, "중복된 로그인 ID로 회원가입 시 예외가 발생해야 합니다.");
    }
}

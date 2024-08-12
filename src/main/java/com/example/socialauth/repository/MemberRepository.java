package com.example.socialauth.repository;

import com.example.socialauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByLoginId(String loginId);
    Member findByLoginIdAndLoginType(String loginId, Member.LoginType loginType);
}

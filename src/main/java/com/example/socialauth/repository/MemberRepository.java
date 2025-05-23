package com.example.socialauth.repository;

import com.example.socialauth.entity.member.LoginType;
import com.example.socialauth.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByLoginId(String loginId);
    Member findByLoginIdAndLoginType(String loginId, LoginType loginType);
    boolean existsByLoginId(String loginId);
    Member getMemberById(long memberId);
}

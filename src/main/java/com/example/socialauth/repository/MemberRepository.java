package com.example.socialauth.repository;

import com.example.socialauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByGoogleSub(String sub);
    Optional<Member> findByNaverId(String id);
    Optional<Member> findByLoginId(String loginId);
}

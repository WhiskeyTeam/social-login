package com.example.socialauth.repository;

import com.example.socialauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByGoogleSub(String googleSub);
    Optional<Member> findByNaverId(String naverId);
}

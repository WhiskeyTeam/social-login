package com.example.socialauth.repository;

import com.example.socialauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 추가적인 쿼리 메소드가 필요하면 여기에 작성합니다.
}
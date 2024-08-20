package com.example.socialauth.repository;


import com.example.socialauth.entity.member.Member;
import com.example.socialauth.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewer(Member reviewer);

//    List<Review> findByRestaurant(Restaurant restaurant);
//    List<Review> findByProductId(Long productId);
}

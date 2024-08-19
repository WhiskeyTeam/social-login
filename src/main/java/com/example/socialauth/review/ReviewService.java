package com.example.socialauth.review;


import com.example.socialauth.entity.Member;
import com.example.socialauth.entity.review.Review;
import com.example.socialauth.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 아이디로 리뷰 객체 조회
     * @param reviewId 리뷰 아이디
     * @return 리뷰 엔티티 객체
     */
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * 회원 객체로 리뷰 리스트 조회
     * @param member 회원 객체
     * @return 리뷰 리스트
     */
    public List<Review> getReviewsByMember(Member member) {
        return reviewRepository.findByReviewer(member);
    }

//    /**
//     * 음식점 객체로 리뷰 리스트 조회
//     * @param restaurant 음식점 객체
//     * @return 리뷰 리스트
//     */
//    public List<Review> getReviewsByRestaurant(Restaurant restaurant) {
//        return reviewRepository.findByRestaurant(restaurant);
//    }

    // TODO. Resource 담당자쪽 service에 구현되도록 업무요청 전달 -> 컨트롤러 모듈에서 받아 처리할 예정
    // DESC. 2024/08/11 19:27 - 업무요청 완료

    /**
     * 리뷰 작성 / 수정
     * @param review 리뷰 객체
     */
    @Transactional
    public Review saveReview(Review review) {
        reviewRepository.save(review);
        return review;
    }

    /**
     * 리뷰 삭제
     * @param review 리뷰 객체(반드시 영속성 상태의 객체가 전달되어야 함)
     */
    @Transactional
    public void removeReview(Review review) {
        reviewRepository.delete(review);
    }

//    /**
//     * 리뷰에 좋아요 추가
//     * @param review 리뷰 객체
//     * @param reviewLike 리뷰 좋아요 객체
//     */
//    @Transactional
//    public void addLikeToReview(Review review, ReviewLike reviewLike) {
//        review.getLikes().add(reviewLike);
//    }
//
//    /**
//     * 음식점 당 총 Review-Rating 값의 평균치를 계산하여 반환
//     * @param restaurant 음식점 객체
//     * @return 음식점에 대한 리뷰 평균 평점
//     */
//    public double getAverageRatingForRestaurant(Restaurant restaurant) {
//        List<Review> reviews = getReviewsByRestaurant(restaurant);
//        return reviews.stream()
//                .mapToInt(review -> review.getRating().getValue())
//                .average()
//                .orElse(0);
//    }
}

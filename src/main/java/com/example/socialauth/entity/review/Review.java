package com.example.socialauth.entity.review;


import com.example.socialauth.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Table(name = "tbl_review")
@Entity
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 리뷰 식별자

    // TODO 데이터베이스에 Value 값이 저장되게 Converter 어노테이션과 클래스 생성해야함
    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    private Rating rating;  // 별점

    @Column(nullable = false)
    private String content; // 리뷰 내용

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;    // 리뷰 작성시간

    private LocalDateTime deletedAt;    // 리뷰 삭제시간

    @Column(nullable = false)
    private boolean isSuspended; // 노출 여부

//    @ManyToOne
//    @JoinColumn(name = "restaurant_id", nullable = false)
//    private Restaurant restaurant;  // 음식점 객체의 정보

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Member reviewer;    // 리뷰 작성자

//    @OneToMany(mappedBy = "review", fetch = FetchType.EAGER)
//    private List<ReviewLike> likes; // 좋아요 리스트

//    @OneToOne
//    @JoinColumn(name = "receipt_data_id", nullable = false)
//    private ReceiptData receiptData;    // 영수증 정보
}
package com.example.socialauth.entity.resource;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Table(name = "tbl_image_file")
@Entity
@Getter
@Setter
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;    // 원본 파일명

    @Column(nullable = false)
    private String uuidFileName;    // UUID 파일명

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime uploadedAt;    // 업로드 시점에 현재 일시로 초기화
}